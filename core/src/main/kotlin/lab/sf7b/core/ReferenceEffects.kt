package lab.sf7b.core

import kotlin.math.*

/** Independent implementations of mechanisms observed in the reference APKs.
 * Their seeded PRNG and control ranges are sf7b choices; runtime pixel parity is unverified.
 * Old documents use algorithmVersion 1 and never pass through this dispatch. */
object ReferenceEffects {
    val ids = setOf("patchwork_break", "random_pixelate", "low_freq_banding")
    private fun interface PixelOperation { fun pixel(x: Float, y: Float): Int }

    fun render(pool: SurfacePool, input: Surface, effect: Effect, width: Int, height: Int,
               docWidth: Int, docHeight: Int, check: () -> Unit = {},reference:Surface?=null): Surface? {
        if (effect.algorithmVersion != 2 || effect.id !in ids) return null
        require(width == input.width && height == input.height && docWidth > 0 && docHeight > 0)
        val pixel: PixelOperation = when (effect.id) {
            "random_pixelate" -> pixelate(input, effect, docWidth, docHeight,check)
            "patchwork_break" -> patchwork(input, effect, docWidth, docHeight,check)
            else -> angularPick(input,reference?:input, effect, docWidth, docHeight)
        }
        val result = pool.create(width, height)
        val dx = docWidth.toFloat() / width; val dy = docHeight.toFloat() / height
        for (y in 0 until height) {
            check()
            val yy = (y + .5f) * dy
            val row = IntArray(width) { x -> pixel.pixel((x + .5f) * dx, yy) }
            result.writeRow(y, row)
        }
        result.seal()
        return result
    }

    private class Cell(val left: Float, val top: Float, val right: Float, val bottom: Float,
                       val key: Long, val axisY: Boolean = false, val split: Float = 0f,
                       val first: Cell? = null, val second: Cell? = null) {
        val cx get() = (left + right) * .5f
        val cy get() = (top + bottom) * .5f
        val width get() = right - left
        val height get() = bottom - top
        fun find(x: Float, y: Float): Cell {
            var cell = this
            while (cell.first != null) cell = if ((if (cell.axisY) y else x) < cell.split) cell.first!! else cell.second!!
            return cell
        }
    }

    /** Every split depends on its path, not raster size, tile boundaries or visiting order. */
    private fun partition(width: Int, height: Int, e: Effect, seed: Long,check:()->Unit): Cell {
        val depthLimit = e.p("depth").roundToInt().coerceIn(0, 12)
        val regularity = e.p("regularity").coerceIn(0f, 100f)
        var visited=0
        fun build(left: Float, top: Float, right: Float, bottom: Float, key: Long, depth: Int): Cell {
            if(++visited%128==0)check()
            val w = right - left; val h = bottom - top
            if (depth >= depthLimit || w <= 1f || h <= 1f) return Cell(left, top, right, bottom, key)
            val alternated = depth % 2 == 0 // Y first, then alternating.
            val axisY = if (random(seed, key, 0) < regularity * .02f) h >= w else alternated
            val bias = e.p("bias") / (1 shl depth)
            val randomFraction = random(seed, key, 1).toDouble().pow(2.0.pow(-bias.toDouble())).toFloat()
            val regularMix = ((regularity - 50f) / 50f).coerceIn(0f, 1f)
            val fraction = (randomFraction * (1 - regularMix) + .5f * regularMix).coerceIn(.01f, .99f)
            val split = if (axisY) top + h * fraction else left + w * fraction
            val a = if (axisY) build(left, top, right, split, key * 2, depth + 1) else build(left, top, split, bottom, key * 2, depth + 1)
            val b = if (axisY) build(left, split, right, bottom, key * 2 + 1, depth + 1) else build(split, top, right, bottom, key * 2 + 1, depth + 1)
            return Cell(left, top, right, bottom, key, axisY, split, a, b)
        }
        return build(0f, 0f, width.toFloat(), height.toFloat(), 1, 0)
    }

    private fun sample(source: Surface, x: Float, y: Float, docWidth: Int, docHeight: Int): Int =
        source.sample((x / docWidth * source.width - .5f).coerceIn(0f,(source.width-1).toFloat()),
            (y / docHeight * source.height - .5f).coerceIn(0f,(source.height-1).toFloat()))

    private fun border(source: Int, cell: Cell, x: Float, y: Float, e: Effect, docHeight: Int): Int {
        val thickness = e.p("thickness") * .001f * docHeight * .5f
        if (thickness <= 0f || minOf(x - cell.left, cell.right - x, y - cell.top, cell.bottom - y) > thickness) return source
        val color = e.palette.firstOrNull() ?: 0
        val straight = Pixels.straight(source); val amount = Pixels.a(color) / 255f
        fun component(a: Int, b: Int) = ((a * (1 - amount) + b * amount).roundToInt()).coerceIn(0, 255)
        val mixed = (Pixels.a(straight) shl 24) or (component(Pixels.r(straight), Pixels.r(color)) shl 16) or
            (component(Pixels.g(straight), Pixels.g(color)) shl 8) or component(Pixels.b(straight), Pixels.b(color))
        return Pixels.premul(mixed)
    }

    private fun pixelate(source: Surface, e: Effect, docWidth: Int, docHeight: Int,check:()->Unit): PixelOperation {
        val tree = partition(docWidth, docHeight, e, e.seed,check)
        val intensity = e.p("intensity")
        val scale = 1.01.pow(intensity.toDouble()).toFloat()
        return PixelOperation { x, y ->
            val cell = tree.find(x, y)
            val qx = if (intensity == 0f) cell.cx else cell.cx + (x - cell.cx) * scale
            val qy = if (intensity == 0f) cell.cy else cell.cy + (y - cell.cy) * scale
            var color = sample(source, qx, qy, docWidth, docHeight)
            if (e.p("palette") >= 1f && e.palette.size > 1) {
                val c = Pixels.straight(color)
                var nearest = e.palette.first(); var shortest = Long.MAX_VALUE
                for(candidate in e.palette){val difference=distance(c,candidate);if(difference<shortest){shortest=difference;nearest=candidate}}
                color = Pixels.premul((Pixels.a(c) shl 24) or (nearest and 0x00ffffff))
            }
            border(color, cell, x, y, e, docHeight)
        }
    }

    private fun patchwork(source: Surface, e: Effect, docWidth: Int, docHeight: Int,check:()->Unit): PixelOperation {
        val mode = e.p("mode").roundToInt().coerceIn(0, 10)
        val repeat = e.p("count").roundToInt().coerceIn(1, 16)
        val trees = Array(repeat) { i -> partition(docWidth, docHeight, e, if (mode in listOf(1, 8, 9)) e.seed else e.seed + i,check) }
        val intensity = e.p("intensity"); val gain = intensity * abs(intensity) * .01f
        val scale = 1.05.pow(gain.toDouble()).toFloat()
        val radians = gain * .1f; val cosine = cos(radians); val sine = sin(radians)
        return PixelOperation { x, y ->
            var qx = x; var qy = y
            var lastX = x; var lastY = y
            var last = trees[0].find(x, y)
            for (i in trees.indices) {
                val cell = trees[i].find(qx, qy); last = cell; lastX = qx; lastY = qy
                val cycleSeed=if(mode==1||mode==8||mode==9)e.seed else e.seed+i
                val rx = random(cycleSeed, cell.key, 4) * 2f - 1f
                val ry = random(cycleSeed, cell.key, 5) * 2f - 1f
                when (mode) {
                    0, 1 -> { qx = cell.cx + (qx - cell.cx) * scale; qy = cell.cy + (qy - cell.cy) * scale }
                    2 -> qx += rx * gain * .02f * docWidth
                    3 -> qy += ry * gain * .02f * docHeight
                    4 -> if (abs(rx) > abs(ry)) qx += rx * gain * .02f * docWidth else qy += ry * gain * .02f * docHeight
                    5 -> if (cell.width > cell.height) qx += rx * gain * .02f * docWidth else qy += ry * gain * .02f * docHeight
                    6 -> { qx = cell.cx + (qx - cell.cx) / scale; qy = cell.cy + (qy - cell.cy) / scale }
                    7, 8 -> if (cell.width > cell.height) qx += rx * gain * .02f * cell.width else qy += ry * gain * .02f * cell.height
                    9 -> { qx += rx * gain * .0005f * cell.width; qy += ry * gain * .0005f * cell.height }
                    else -> { val dx = qx - cell.cx; val dy = qy - cell.cy; qx = cell.cx + cosine * dx - sine * dy; qy = cell.cy + sine * dx + cosine * dy }
                }
            }
            border(sample(source, qx, qy, docWidth, docHeight), last, lastX, lastY, e, docHeight)
        }
    }

    /** ColorPickAngular: select the nearest RGBA boundary sample, rather than generating bands. */
    private fun angularPick(source: Surface, reference:Surface,e: Effect, docWidth: Int, docHeight: Int): PixelOperation {
        val count = e.p("count").roundToInt().coerceIn(2, 64)
        val phase = e.p("angle") * PI / 180
        val fullCircle = e.p("reference") < .5f // 0 = Glitch Lab sweep; 1 = observed One Lab half-circle sweep.
        val rays = Array(count) { i -> val angle = phase + i * (if (fullCircle) 2 * PI else PI) / count; cos(angle).toFloat() to sin(angle).toFloat() }
        val rotation = e.p("rotation") * PI / 180; val cosine = cos(rotation).toFloat(); val sine = sin(rotation).toFloat()
        val cellW = max(1f, e.p("cell_size") * docWidth); val cellH = max(1f, e.p("cell_size") * docHeight)
        val offsetX = e.p("offset_x") * docWidth; val offsetY = e.p("offset_y") * docHeight
        return PixelOperation { x, y ->
            val centeredX = x - docWidth * .5f; val centeredY = y - docHeight * .5f
            val localX = centeredX * cosine + centeredY * sine - offsetX
            val localY = -centeredX * sine + centeredY * cosine - offsetY
            val left = floor((localX + cellW * .5f) / cellW) * cellW - cellW * .5f
            val top = floor((localY + cellH * .5f) / cellH) * cellH - cellH * .5f
            val original = sample(source, x, y, docWidth, docHeight)
            val originalStraight = Pixels.straight(original)
            var best = original; var bestDistance = Long.MAX_VALUE
            for ((dx, dy) in rays) {
                val tx = if (abs(dx) < .000001f) Float.POSITIVE_INFINITY else ((if (dx > 0) left + cellW else left) - localX) / dx
                val ty = if (abs(dy) < .000001f) Float.POSITIVE_INFINITY else ((if (dy > 0) top + cellH else top) - localY) / dy
                val t = min(tx, ty)
                if (t >= 0f && t.isFinite()) {
                    val lx = localX + dx * t + offsetX; val ly = localY + dy * t + offsetY
                    val qx = lx * cosine - ly * sine + docWidth * .5f
                    val qy = lx * sine + ly * cosine + docHeight * .5f
                    val candidate = sample(reference, qx, qy, docWidth, docHeight)
                    val difference = distance(originalStraight, Pixels.straight(candidate))
                    if (difference < bestDistance) { best = candidate; bestDistance = difference }
                }
            }
            best
        }
    }

    private fun distance(a: Int, b: Int): Long {
        val dr = Pixels.r(a) - Pixels.r(b); val dg = Pixels.g(a) - Pixels.g(b)
        val db = Pixels.b(a) - Pixels.b(b); val da = Pixels.a(a) - Pixels.a(b)
        return (dr * dr + dg * dg + db * db + da * da).toLong()
    }
    private fun random(seed: Long, key: Long, lane: Int): Float {
        var value = seed xor (key * -7046029254386353131L) xor (lane.toLong() * -4658895280553007687L)
        value = (value xor (value ushr 30)) * -4658895280553007687L
        value = (value xor (value ushr 27)) * -7723592293110705685L
        return ((value xor (value ushr 31)) ushr 40).toFloat() / 16777216f
    }
}

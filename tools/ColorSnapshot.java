import lab.sf7b.core.*;
import kotlin.Unit;
import java.nio.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Own legacy CPU images, covering every valid alpha/channel pair, before a kernel optimization. */
public class ColorSnapshot {
    public static void main(String[] args) throws Exception {
        int size=256;int[][] fixture=new int[size][size];
        for(int y=0;y<size;y++)for(int x=0;x<size;x++)fixture[y][x]=Pixels.INSTANCE.rgba(x/255f,((x*73+y*13)&255)/255f,((x*29+y*17)&255)/255f,y/255f);
        Document base=Codec.INSTANCE.decode("""
            {"version":1,"name":"Color snapshot","width":256,"height":256,"layers":[{"id":"fixture","kind":"image","asset":"assets/fixture.png","head":"source-fixture"}],"nodes":[{"id":"source-fixture","kind":"source","layer":"fixture"}],"root":"source-fixture"}
            """);
        Path temporary=Files.createTempDirectory("sf7b-color-snapshot-").toRealPath();
        StringBuilder csv=new StringBuilder("effect,variant,pixels,sha256\n");
        try {
            for(String id:List.of("adjust","invert","threshold","posterize","curves","gradient_map","palette","scanlines","rgb_split"))for(int variant=0;variant<8;variant++) {
                Random random=new Random(17477L+variant*313L+id.hashCode());Effect defaults=Catalog.INSTANCE.newEffect(id);
                Map<String,Float> parameters=new LinkedHashMap<>();
                if(variant>0)for(Param p:Catalog.INSTANCE.byId(id).getParams()) {
                    float value=Math.max(p.getMin(),Math.min(p.getMax(),p.getDefault()+(random.nextFloat()-.5f)*(p.getMax()-p.getMin())*.6f));
                    parameters.put(p.getId(),p.getIntegral()?(float)Math.round(value):value);
                }
                List<List<Point>> curves=defaults.getCurves();
                if(id.equals("curves")&&variant>0){curves=new ArrayList<>();for(int channel=0;channel<4;channel++)curves.add(List.of(new Point(0f,0f),new Point(.27f,.1f+random.nextFloat()*.8f),new Point(.71f,.1f+random.nextFloat()*.8f),new Point(1f,1f)));}
                List<Integer> palette=defaults.getPalette();
                if(variant>0&&(id.equals("palette")||id.equals("gradient_map"))){palette=new ArrayList<>();for(int k=0;k<4;k++)palette.add((((variant*47+k*73)&255)<<24)|(random.nextInt()&0xffffff));}
                Effect effect=defaults.copy(id,parameters,7L,true,variant==0?1f:.16f+variant*.1f,palette,curves,null,defaults.getAlgorithmVersion());
                Document doc=base.addEffect("fixture",effect,null);doc.validate();
                try(SurfacePool pool=new SurfacePool(temporary.resolve(id+"-"+variant).toFile(),128,8*1024*1024,true,false)) {
                    Surface source=pool.create(size,size);for(int y=0;y<size;y++)source.writeRow(y,fixture[y]);source.seal();
                    SourceProvider provider=new SourceProvider(){
                        public Surface image(String asset){return source;}
                        public Surface image(String asset,int width,int height){return source;}
                        public Surface text(Layer layer,int width,int height){throw new IllegalStateException("unused");}
                        public String resourceVersion(String asset){return "own-alpha-fixture-v1";}
                    };
                    Surface rendered=new Renderer(pool,provider,()->Unit.INSTANCE,null,value->Unit.INSTANCE).render(doc,size,size);
                    MessageDigest digest=MessageDigest.getInstance("SHA-256");ByteBuffer row=ByteBuffer.allocate(size*4).order(ByteOrder.LITTLE_ENDIAN);
                    for(int y=0;y<size;y++){row.clear();row.asIntBuffer().put(rendered.readRow(y));digest.update(row.array());}
                    csv.append(id).append(',').append(variant).append(',').append(size*size).append(',').append(HexFormat.of().formatHex(digest.digest())).append('\n');
                }
            }
            Files.writeString(Path.of(args[0]),csv);System.out.println("Recorded 72 color snapshots, "+72L*size*size+" pixels: "+args[0]);
        } finally {
            Path parent=Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
            if(!temporary.getParent().equals(parent)||!temporary.getFileName().toString().startsWith("sf7b-color-snapshot-"))throw new IllegalStateException("Unexpected cleanup target");
            try(var paths=Files.walk(temporary)){for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);}
        }
    }
}

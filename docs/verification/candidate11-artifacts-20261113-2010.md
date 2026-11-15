# Пункт1.4 — отдельные APK, SHA и сертификаты

13.11.2026 20:10МСК. **Пункт1.4 завершён.** Архивированы outputs единственного успешного запуска1.3 (19:54:22–20:00:55МСК, BUILD SUCCESSFUL/exit0). Gradle/тесты заново не запускались, установки и обращения к телефону не было.

## Артефакты

| Файл | Байты | SHA256 |
|---|---:|---|
| candidate11.apk (материал отсутствует в этой копии) | 12621793 | `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500` |
| candidate11-tests.apk (материал отсутствует в этой копии) | 1173964 | `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a` |

Манифест сборки/артефактов (материал отсутствует в этой копии), SHA256SUMS (материал отсутствует в этой копии). SHA обоих build-root originals вычислены до копирования и повторно после; сохранённые APK совпали побайтно. Размеры/время originals и output-metadata совпали с сохранёнными в1.3.71файл source/config/tools совпал с checkpoint1.2; новой сборки/изменений кода между1.3 и архивированием не было.

Это debug0.2.0/code2, package `lab.sf7b`. Test package `lab.sf7b.test`, instrumentation `androidx.test.runner.AndroidJUnitRunner`, targetPackage `lab.sf7b`. Test manifest не задаёт versionCode/versionName (значение0 в Gradle output-metadata служебное). APK-кандидаты1–10 и старый выданный debug не заменялись.

## Подписи

Оба **сохранённых** APK прошли `apksigner verify --verbose --print-certs-pem`, exit0, verified v2, один signer. Общий сертификат SHA256:

`dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`

Он совпадает с проверенным в1.2 сертификатом candidate10/выданного APK. Полные публичные сертификаты сохранены: app PEM (материал отсутствует в этой копии), test PEM (материал отсутствует в этой копии). После декодирования PEM SHA256 DER независимо совпал с отпечатком apksigner; оба сертификата одинаковы. Это публичные сертификаты, закрытый ключ/keystore/пароли в артефакты и отчёт не включены.

Сохранены app signature (материал отсутствует в этой копии), test signature (материал отсутствует в этой копии), badging обоих APK и XMLtree test manifest.

## Связь с проверками и ограничения

Манифест содержит SHA256 **19 файлов доказательств**: журнал1.3, coreXML, Lint, output-metadata, run/result JSON, итог1.3 и новые signature/badging/test manifest. Таким образом результаты1.3 относятся к зафиксированным здесь байтам:

- Core71case:70passed/1skipped/0failures/0errors; пропущен выключенный largeCanvasBenchmarks.
- App/testAPK собраны, Lint0errors/18warnings.
- Большой benchmark, instrumentation tests и A51-корректность candidate11 ещё не проверены. Совпадение сертификата подтверждает возможность обновления по подписи, не успешность миграции.
- Телефон в1.4 не затрагивался; последнее наблюдение установленной версии — candidate10 в1.2. Private backup19:45:26МСК сохранён отдельно; если пользователь редактировал проекты позже, перед2.1 нужна свежая копия.
- Исторический result1.3 с полями apk_archived=false сохранён неизменным: это состояние на конец1.3. Текущий факт архивации и SHA находятся в новом манифесте1.4.

Перепроверка подписей сохранённых APK без установки:

```powershell
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -jar '<local-private-path>' verify --verbose --print-certs '<repository>\artifacts\sf7b-lab-0.2.0-candidate11.apk'
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -jar '<local-private-path>' verify --verbose --print-certs '<repository>\artifacts\sf7b-lab-0.2.0-candidate11-tests.apk'
```

**Сейчас пауза. Следующий2.1 — проверить A51/backup, установить именно эти APK обновлением без clear/uninstall, выполнить MixedDocumentDeviceTest и зафиксировать результат сохранности.**

# GitHub ile modu derleme (kurulum yok, tarayıcıdan)

1. github.com adresinde hesabın yoksa ücretsiz hesap aç, giriş yap.
2. Sağ üstteki **+** düğmesinden **New repository**'ye bas. İsim olarak `takip-modu` yaz, **Public** seç (ücretsiz derleme için en kolayı), **Create repository**'ye bas.
3. Açılan sayfada **uploading an existing file** bağlantısına tıkla.
4. Bu zip'i bilgisayarında bir klasöre çıkar. Klasörün **içindeki her şeyi** (`src` klasörü, `build.gradle`, `gradle.properties`, `settings.gradle`, `.github` klasörü dahil) tarayıcıdaki sayfaya sürükle bırak. Zip'i olduğu gibi yükleme.
5. Aşağıda **Commit changes**'e bas.
6. Üstteki **Actions** sekmesine gir. "Modu derle" çalışmaya başlar (yaklaşık 3-5 dakika). Yeşil tik çıkınca o çalışmanın üstüne tıkla.
7. Sayfanın altındaki **Artifacts** bölümünde `tekrarbot-mod` dosyasını indir. İçinden `tekrarbot-1.0.0.jar` çıkar. Bu dosyayı `.minecraft\mods` klasörüne at.
8. Fabric API jar'ını (`fabric-api-0.165.0+26.3`) Modrinth'ten indirip aynı klasöre koy.

## Actions sekmesinde "Modu derle" görünmezse

Bazı sistemler `.github` gibi nokta ile başlayan klasörü sürükle bırakta atlayabilir. Şunu yap:

1. Deponun ana sayfasında **Add file > Create new file**'a bas.
2. Dosya adı kutusuna tam olarak `.github/workflows/build.yml` yaz (eğik çizgiler klasör oluşturur).
3. Aşağıdaki içeriği içine yapıştır ve **Commit changes**'e bas. Sonra Actions sekmesinden **Modu derle > Run workflow**'a bas.

```yaml
name: Modu derle

on:
  workflow_dispatch:
  push:
    branches: [main, master]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Dosyalari al
        uses: actions/checkout@v4

      - name: Java 25 kur
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'

      - name: Gradle kur
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.6.0'

      - name: Derle
        run: gradle build --no-daemon

      - name: Jar dosyasini yukle
        uses: actions/upload-artifact@v4
        with:
          name: tekrarbot-mod
          path: build/libs/tekrarbot-1.0.0.jar
          if-no-files-found: error
```

## Derleme kırmızı (başarısız) olursa

Actions sayfasında başarısız çalışmaya tıkla, **Derle** adımını aç ve en alttaki kırmızı hata satırlarını kopyalayıp bana gönder. Kodu hiç derlemediğim için bir isim hatası çıkması olası, düzeltip yeni sürümü veririm.

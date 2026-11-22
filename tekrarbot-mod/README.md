# Takip Modu (Fabric istemci modu, Minecraft 26.3, TLauncher)

İkinci bilgisayardaki gerçek oyun karakterin seni 1-2 blok mesafede takip eder.
Ayarlar oyun içinden değil, oyunla birlikte açılan küçük bir **panel penceresinden** yapılır.
Mod başlatıcıdan bağımsızdır, TLauncher ile de çalışır.

## Gerekenler (ikinci bilgisayar)

- TLauncher
- Fabric Loader 0.19.5 (Minecraft 26.3 için)
- Fabric API 0.165.0+26.3
- Bu mod (`tekrarbot-1.0.0.jar`)
- Java 25
- Windows veya Linux önerilir. macOS'ta ayrı pencere açma sorun çıkarabilir.

## TLauncher ile kurulum

1. Fabric Loader'ı resmi Fabric yükleyicisiyle (fabricmc.net) Minecraft 26.3 için kur. Yükleyici `.minecraft/versions` klasörüne bir Fabric sürümü ekler, TLauncher bu klasörü okuduğu için sürüm listesinde görünür. TLauncher kendi sürüm listesinde Fabric'i 26.3 için sunuyorsa onu da seçebilirsin.
2. TLauncher'da Fabric 26.3 sürümünü seç.
3. `tekrarbot-1.0.0.jar` ve Fabric API jar dosyasını `.minecraft/mods` klasörüne at (Windows: `%appdata%\.minecraft\mods`).
4. TLauncher ayarlarında Java yolu Java 25'i göstermeli. TLauncher otomatik indirmezse JDK 25 kurup yolunu elle ver.
5. TLauncher'da ana hesabından **farklı** bir kullanıcı adıyla gir.

## Sunucu ayarı (kendi sunucun için)

TLauncher hesapları Microsoft hesabı değildir (çevrimdışı hesap). Sunucunun `server.properties` dosyasında
`online-mode=false` olmalı, yoksa ikinci hesap giremez. Bu ayar sunucuyu kimlik doğrulamasız bırakır,
bu yüzden sunucuda AuthMe gibi bir giriş eklentisi ve beyaz liste (`white-list=true`) kullanman iyi olur.
Çevrimdışı modda oyuncu adı yeterli olduğu için, birinin senin adını taklit etmemesi için bu önlemler önemli.

## Derleme

1. JDK 25 kur.
2. Bu klasörde: `gradle build` (Gradle 9.6)
3. `build/libs/tekrarbot-1.0.0.jar` dosyasını yukarıdaki `mods` klasörüne at.

## Kullanım

Oyunu açınca **Takip Paneli** penceresi ayrıca açılır (her zaman üstte).

| Alan | Ne yapar |
|---|---|
| Takip edilecek oyuncu | Ana hesabının kullanıcı adı |
| Mesafe (blok) | Kaç blok yakınında dursun (1-10, varsayılan 2) |
| Başlat / Durdur | Takibi açar veya kapatır |
| Durum | Oyuncu görüyor mu, takip ediyor mu |

Panel kapatma düğmesi pencereyi kapatmaz, modu durdurmak için "Durdur"a bas.

## Nasıl çalışır

Hedef oyuncuya dönüp yürür. Mesafe ayarın kadar yaklaşınca durur. Uzaktaysa koşar, hedef yüksekteyse veya önünde engel varsa zıplar.
Hedef oyuncu görüş mesafesinde (yüklenmiş) olmalı, yoksa bekler.

## Sınırlar

- Karmaşık yollarda, suda veya dar yerlerde takılabilir. Işınlanma yoktur.
- Vurma, kırma, blok koyma gibi eylemler yapmaz, sadece takip eder.

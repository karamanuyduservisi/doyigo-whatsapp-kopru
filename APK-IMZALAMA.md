# DoyiGo WhatsApp Köprü – sürüm yükseltme

Kalıcı APK güncellemesi için packageName ve imza anahtarı değişmemelidir. Android versionCode her sürümde yükseltilmelidir.

GitHub Actions'ın debug APK imzası her derleme makinesinde farklı olabileceğinden, debug APK dosyasını kullanıcıya dağıtma. Kalıcı anahtarla imzalanan APK dağıtılmalıdır.

İlk kalıcı imza sürümüne geçerken önceki geçici imzalı test uygulamasının bir defa kaldırılması gerekebilir. Daha sonraki sürümler aynı imza anahtarıyla yerinde güncellenir.

İmzalama anahtarını asla GitHub deposuna veya workflow dosyasına koyma. Gizli anahtarı kaybolmayacak şekilde güvenli sakla.

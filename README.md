# DoyiGo WhatsApp Köprü – Güvenli test sürümü

Android uygulama kimliği: `com.doyigo.whatsappbridge.client` (0.5 ve sonrası).

v0.6: Google Play Protect engeline yol açan Erişilebilirlik hizmeti ve erişim izni kaldırıldı. WhatsApp, WhatsApp Business tercihi kaydedilir; ancak telefonun klon uygulama seçimini Android sistemi yönetir ve her açılışta sorabilir.

Test mesajı uygulama içinden hazırlanır ve WhatsApp'ta kullanıcı onayıyla gönderilir. **Bu uygulama otomatik gönderim yapmaz.** Tam otomatik WhatsApp sipariş bildirimleri için resmi WhatsApp Business Platform API entegrasyonu gerekir.

Bundan sonraki güncellemelerde paket kimliği, imza anahtarı aynı tutulmalı ve versionCode artırılmalıdır. Önceki 0.5 kullanıcıları uygun imzalı güncelleme dosyasını doğrudan kurabilirler.

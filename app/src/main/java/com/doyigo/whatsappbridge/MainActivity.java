package com.doyigo.whatsappbridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
  private static final String PREFS = "dg_wa_bridge";
  private static final String NORMAL = "com.whatsapp";
  private static final String BUSINESS = "com.whatsapp.w4b";
  private static final String ASK = "ask";
  private static final String DEFAULT_MESSAGE =
      "DoyiGo: Yeni siparişiniz var. Detayları restoran panelinden inceleyebilirsiniz.";
  private static final int ORANGE = 0xfff0643d;
  private SharedPreferences prefs;
  private TextView chosenLabel;
  private EditText phoneInput;
  private EditText testInput;

  @Override public void onCreate(Bundle saved) {
    super.onCreate(saved);
    prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
    getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    setContentView(createScreen());
    refreshChosenLabel();
  }

  @Override protected void onResume() {
    super.onResume();
    if (chosenLabel != null && prefs != null) refreshChosenLabel();
  }

  @Override protected void onPause() {
    saveDraft();
    super.onPause();
  }

  private View createScreen() {
    LinearLayout screen = new LinearLayout(this);
    screen.setOrientation(LinearLayout.VERTICAL);
    screen.setBackgroundColor(0xfff6f8fb);

    LinearLayout topBar = new LinearLayout(this);
    topBar.setOrientation(LinearLayout.HORIZONTAL);
    topBar.setGravity(Gravity.CENTER_VERTICAL);
    topBar.setPadding(dp(20), dp(18), dp(16), dp(13));
    topBar.setBackgroundColor(Color.WHITE);
    LinearLayout nameBox = new LinearLayout(this);
    nameBox.setOrientation(LinearLayout.VERTICAL);
    nameBox.addView(label("DoyiGo", 15, ORANGE, true));
    nameBox.addView(label("WhatsApp Köprü", 22, 0xff202b3a, true));
    topBar.addView(nameBox, new LinearLayout.LayoutParams(0, -2, 1f));
    Button settings = button("Ayarlar", false);
    settings.setOnClickListener(v -> showSettings());
    topBar.addView(settings, new LinearLayout.LayoutParams(-2, dp(44)));
    screen.addView(topBar);

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(false);
    scroll.setClipToPadding(false);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(dp(20), dp(24), dp(20), dp(30));

    TextView sub = label("Restoran bildirimi · Test ekranı", 14, 0xff67768a, false);
    content.addView(sub);
    space(content, 21);

    LinearLayout selectedCard = new LinearLayout(this);
    selectedCard.setOrientation(LinearLayout.VERTICAL);
    selectedCard.setPadding(dp(16), dp(15), dp(16), dp(15));
    selectedCard.setBackground(card(Color.WHITE, 15, 0xffe4e8ef));
    selectedCard.addView(label("MESAJIN GİDECEĞİ UYGULAMA", 11, 0xff8994a3, true));
    space(selectedCard, 8);
    chosenLabel = label("Seçilmedi", 17, 0xff233246, true);
    selectedCard.addView(chosenLabel);
    space(selectedCard, 7);
    selectedCard.addView(label("Ayarlar'dan istediğin zaman değiştirebilirsin.", 12, 0xff748195, false));
    selectedCard.setOnClickListener(v -> showSettings());
    content.addView(selectedCard);
    space(content, 24);

    content.addView(label("Restoran telefonu", 17, 0xff243144, true));
    space(content, 8);
    phoneInput = input("05XX XXX XX XX", InputType.TYPE_CLASS_PHONE);
    phoneInput.setSingleLine(true);
    phoneInput.setText(prefs.getString("phone", ""));
    content.addView(phoneInput, new LinearLayout.LayoutParams(-1, dp(54)));
    space(content, 8);
    content.addView(label("Test mesajının gönderileceği numara.", 12, 0xff798599, false));
    space(content, 22);

    LinearLayout info = new LinearLayout(this);
    info.setOrientation(LinearLayout.VERTICAL);
    info.setPadding(dp(15), dp(15), dp(15), dp(15));
    info.setBackground(card(0xfffff4e7, 13, 0xffffedce));
    info.addView(label("Nasıl çalışır?", 14, 0xff7f531f, true));
    space(info, 6);
    info.addView(label("Test mesajını yaz. Bir kere Erişilebilirlik izni verdikten sonra WhatsApp'ta gönderme düğmesine otomatik basmayı dener.", 13, 0xff805e35, false));
    content.addView(info);
    scroll.addView(content);
    screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

    LinearLayout composer = new LinearLayout(this);
    composer.setOrientation(LinearLayout.VERTICAL);
    composer.setPadding(dp(18), dp(13), dp(18), dp(18));
    composer.setBackground(card(Color.WHITE, 18, 0xffe6eaf1));
    composer.setElevation(dp(8));
    composer.addView(label("TEST MESAJI", 12, 0xff677487, true));
    space(composer, 8);
    testInput = input("Mesajını yaz...", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    testInput.setMinLines(2);
    testInput.setMaxLines(4);
    testInput.setGravity(Gravity.TOP);
    testInput.setText(prefs.getString("draft", DEFAULT_MESSAGE));
    testInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
    testInput.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendTest();
        return true;
      }
      return false;
    });
    composer.addView(testInput);
    space(composer, 10);
    Button send = button("Test Mesajını Gönder", true);
    send.setOnClickListener(v -> sendTest());
    composer.addView(send, new LinearLayout.LayoutParams(-1, dp(50)));
    screen.addView(composer);
    return screen;
  }

  private void saveDraft() {
    if (prefs == null) return;
    SharedPreferences.Editor editor = prefs.edit();
    if (phoneInput != null) editor.putString("phone", phoneInput.getText().toString());
    if (testInput != null) editor.putString("draft", testInput.getText().toString());
    editor.apply();
  }

  private void refreshChosenLabel() {
    String app = prefs.getString("target", ASK);
    String text;
    if (NORMAL.equals(app)) text = "WhatsApp";
    else if (BUSINESS.equals(app)) text = "WhatsApp Business";
    else text = "İlk gönderimde uygulama seç";
    chosenLabel.setText(text);
  }

  private void showSettings() {
    final List<String> names = new ArrayList<>();
    final List<String> packages = new ArrayList<>();
    names.add("Bir sonraki gönderimde yeniden seç");
    packages.add(ASK);
    if (installed(NORMAL)) {
      names.add("WhatsApp");
      packages.add(NORMAL);
    }
    if (installed(BUSINESS)) {
      names.add("WhatsApp Business");
      packages.add(BUSINESS);
    }
    String current = prefs.getString("target", ASK);
    int checked = packages.indexOf(current);
    if (checked < 0) checked = 0;
    new AlertDialog.Builder(this)
      .setTitle("WhatsApp seçimi")
      .setSingleChoiceItems(names.toArray(new String[0]), checked, (dialog, index) -> {
        prefs.edit().putString("target", packages.get(index)).apply();
        refreshChosenLabel();
        dialog.dismiss();
      })
      .setNegativeButton("Kapat", (dialog, which) -> dialog.dismiss())
      .show();
  }

  private void sendTest() {
    String number = normalizePhone(phoneInput.getText().toString());
    if (number == null) {
      phoneInput.setError("Geçerli telefon numarası gir.");
      phoneInput.requestFocus();
      return;
    }
    String body = testInput.getText().toString().trim();
    if (body.length() < 1 || body.length() > 1500) {
      testInput.setError("Mesaj 1–1500 karakter olmalı.");
      return;
    }
    saveDraft();
    if (!autoSendEnabled()) {
      new AlertDialog.Builder(this)
        .setTitle("Otomatik gönderimi etkinleştir")
        .setMessage("DoyiGo Köprü yalnızca senin başlattığın test mesajı için WhatsApp gönder düğmesine basmayı dener. WhatsApp ekranından sunucuya veri aktarmaz. Telefonunun Erişilebilirlik ayarlarında DoyiGo Test Otomatik Gönderim izni gerekir.")
        .setPositiveButton("Ayarlara git", (dialog, which) ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
        .setNegativeButton("Vazgeç", (dialog, which) -> dialog.dismiss())
        .show();
      return;
    }
    String chosen = prefs.getString("target", ASK);
    prefs.edit().putString("pending_msg", body)
      .putString("pending_pkg", ASK.equals(chosen) ? "" : chosen)
      .putLong("pending_started", System.currentTimeMillis())
      .putBoolean("pending_done", false).commit();
    Uri url = Uri.parse("https://wa.me/" + number + "?text=" + Uri.encode(body));
    String selected = prefs.getString("target", ASK);
    Intent intent = new Intent(Intent.ACTION_VIEW, url);
    intent.addCategory(Intent.CATEGORY_BROWSABLE);
    if (!ASK.equals(selected)) {
      if (!installed(selected)) {
        prefs.edit().putString("target", ASK).apply();
        refreshChosenLabel();
      } else {
        intent.setPackage(selected);
        try {
          startActivity(intent);
          return;
        } catch (ActivityNotFoundException | SecurityException ignored) {
          // Installed package may not handle wa.me on this Android device.
          intent.setPackage(null);
        }
      }
    }
    try {
      Intent callback = new Intent(this, ChoiceReceiver.class);
      int flags = PendingIntent.FLAG_UPDATE_CURRENT;
      if (Build.VERSION.SDK_INT >= 31) flags |= PendingIntent.FLAG_MUTABLE;
      PendingIntent chooserCallback = PendingIntent.getBroadcast(this, 104, callback, flags);
      startActivity(Intent.createChooser(intent, "WhatsApp uygulamasını seç", chooserCallback.getIntentSender()));
    } catch (ActivityNotFoundException | SecurityException error) {
      clearPending();
      Toast.makeText(this, "WhatsApp açılamadı. Ayarlar'dan uygulamayı seç.", Toast.LENGTH_LONG).show();
    }
  }

  private void clearPending() {
    prefs.edit().remove("pending_msg").remove("pending_pkg")
      .remove("pending_started").remove("pending_done").apply();
  }

  private boolean autoSendEnabled() {
    AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
    if (manager == null) return false;
    for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
      if (info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null) continue;
      android.content.pm.ServiceInfo si = info.getResolveInfo().serviceInfo;
      if (getPackageName().equals(si.packageName)
          && AutoSendService.class.getName().equals(si.name)) return true;
    }
    return false;
  }

  private boolean installed(String p) {
    try {
      getPackageManager().getPackageInfo(p, 0);
      return true;
    } catch (PackageManager.NameNotFoundException error) {
      return false;
    }
  }

  static String normalizePhone(String raw) {
    String digits = raw.replaceAll("[^0-9]", "");
    if (digits.matches("05[0-9]{9}")) digits = "90" + digits.substring(1);
    else if (digits.matches("5[0-9]{9}")) digits = "90" + digits;
    return digits.matches("[1-9][0-9]{9,14}") ? digits : null;
  }

  /** Android can report which PACKAGE was selected. OEM clone/profile selection
   * is separate and may not be recoverable by third-party apps. */
  public static final class ChoiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      ComponentName component;
      if (Build.VERSION.SDK_INT >= 33)
        component = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT, ComponentName.class);
      else
        component = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
      if (component == null) return;
      String pkg = component.getPackageName();
      if (NORMAL.equals(pkg) || BUSINESS.equals(pkg))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("target", pkg).apply();
    }
  }

  private TextView label(String text, int size, int color, boolean bold) {
    TextView view = new TextView(this);
    view.setText(text);
    view.setTextSize(size);
    view.setTextColor(color);
    if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    return view;
  }

  private EditText input(String hint, int type) {
    EditText view = new EditText(this);
    view.setHint(hint);
    view.setInputType(type);
    view.setTextColor(0xff243246);
    view.setHintTextColor(0xffa2aab7);
    view.setTextSize(15);
    view.setPadding(dp(14), dp(12), dp(14), dp(12));
    view.setBackground(card(Color.WHITE, 12, 0xffdfe5ef));
    return view;
  }

  private Button button(String text, boolean main) {
    Button view = new Button(this);
    view.setAllCaps(false);
    view.setText(text);
    view.setTextSize(14);
    view.setTextColor(main ? Color.WHITE : 0xff26344a);
    view.setBackground(card(main ? ORANGE : 0xffedf1f6, 12, main ? ORANGE : 0xffedf1f6));
    view.setPadding(dp(14), 0, dp(14), 0);
    return view;
  }

  private GradientDrawable card(int fill, int radiusDp, int stroke) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(fill);
    drawable.setCornerRadius(dp(radiusDp));
    drawable.setStroke(dp(1), stroke);
    return drawable;
  }

  private void space(LinearLayout parent, int size) {
    parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(size)));
  }

  private int dp(int px) {
    return (int) (px * getResources().getDisplayMetrics().density + 0.5f);
  }
}

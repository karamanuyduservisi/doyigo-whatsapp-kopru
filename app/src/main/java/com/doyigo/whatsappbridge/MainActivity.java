package com.doyigo.whatsappbridge;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
  private static final String NORMAL = "com.whatsapp";
  private static final String BUSINESS = "com.whatsapp.w4b";
  private final java.util.Map<Integer,String> targets = new java.util.HashMap<>();
  private RadioGroup targetGroup;
  private EditText phone, message;
  private SharedPreferences prefs;

  @Override public void onCreate(Bundle saved) {
    super.onCreate(saved);
    prefs = getSharedPreferences("dg_wa_bridge", MODE_PRIVATE);
    setContentView(screen());
    refreshApplications();
  }

  private View screen() {
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(0xfff7f8fa);
    LinearLayout root = new LinearLayout(this);
    root.setPadding(dp(23),dp(36),dp(23),dp(30));
    root.setOrientation(LinearLayout.VERTICAL);
    scroll.addView(root);
    root.addView(text("DoyiGo", 16, 0xffef633c, true));
    root.addView(text("WhatsApp Köprü", 27, 0xff1b293a, true));
    root.addView(text("Restoran sipariş bildirimi · Deneme", 14, 0xff687586, false));
    gap(root, 26);
    root.addView(text("WhatsApp hesabı", 18, 0xff1b293a, true));
    targetGroup = new RadioGroup(this);
    targetGroup.setOrientation(RadioGroup.VERTICAL);
    root.addView(targetGroup);
    Button refresh = button("Uygulamaları yeniden tara", false);
    refresh.setOnClickListener(v -> refreshApplications());
    root.addView(refresh);
    gap(root, 23);
    root.addView(text("Restoran telefon numarası", 18, 0xff1b293a, true));
    gap(root, 8);
    phone = input("05XX XXX XX XX", InputType.TYPE_CLASS_PHONE, true);
    phone.setText(prefs.getString("phone", ""));
    root.addView(phone);
    gap(root, 17);
    root.addView(text("Mesaj", 18, 0xff1b293a, true));
    gap(root, 8);
    message = input("Mesajı yaz", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, false);
    message.setMinLines(4);
    message.setText("DoyiGo: Yeni siparişiniz var. Detayları restoran panelinden inceleyebilirsiniz.");
    root.addView(message);
    gap(root, 21);
    Button open = button("WhatsApp'ta aç", true);
    open.setOnClickListener(v -> openChat());
    root.addView(open);
    gap(root, 15);
    TextView note = text("Bu ilk sürüm mesajı hazırlar. Mesajı göndermek için WhatsApp'ta gönder düğmesine basmalısın. Canlı DoyiGo sitesine bağlı değildir.", 13, 0xff826333, false);
    note.setPadding(dp(14),dp(13),dp(14),dp(13));
    note.setBackground(bg(0xfffff3dc));
    root.addView(note);
    return scroll;
  }

  private void refreshApplications() {
    targetGroup.removeAllViews();
    targets.clear();
    addOption("Uygulama seçicisi (çift WhatsApp için)", "chooser");
    if (installed(NORMAL)) addOption("WhatsApp", NORMAL);
    if (installed(BUSINESS)) addOption("WhatsApp Business", BUSINESS);
    String chosen = prefs.getString("target", "chooser");
    boolean found = false;
    for (int i=0; i<targetGroup.getChildCount(); i++) {
      int id=targetGroup.getChildAt(i).getId();
      if (chosen.equals(targets.get(id))) { targetGroup.check(id); found=true; break; }
    }
    if (!found) targetGroup.check(targetGroup.getChildAt(0).getId());
    targetGroup.setOnCheckedChangeListener((group,id) -> {
      String target=targets.get(id);
      if (target!=null) prefs.edit().putString("target", target).apply();
    });
  }

  private void addOption(String label, String target) {
    RadioButton radio = new RadioButton(this);
    radio.setId(View.generateViewId());
    radio.setText(label);
    radio.setTextSize(15f);
    radio.setTextColor(0xff25344a);
    radio.setPadding(dp(4),dp(8),0,dp(8));
    targetGroup.addView(radio);
    targets.put(radio.getId(),target);
  }

  private boolean installed(String target) {
    try { getPackageManager().getPackageInfo(target,0); return true; }
    catch (PackageManager.NameNotFoundException err) { return false; }
  }

  private void openChat() {
    String number=normalize(phone.getText().toString());
    String content=message.getText().toString().trim();
    if (number==null) { phone.setError("Geçerli bir telefon numarası gir."); return; }
    if (content.length()<1 || content.length()>1500) {
      message.setError("Mesaj 1–1500 karakter olmalı."); return;
    }
    prefs.edit().putString("phone",phone.getText().toString()).apply();
    Uri url=Uri.parse("https://wa.me/"+number+"?text="+Uri.encode(content));
    Intent target=new Intent(Intent.ACTION_VIEW,url);
    target.addCategory(Intent.CATEGORY_BROWSABLE);
    String selected=targets.get(targetGroup.getCheckedRadioButtonId());
    if (selected!=null && !"chooser".equals(selected)) target.setPackage(selected);
    try {
      if ("chooser".equals(selected) || selected==null)
        startActivity(Intent.createChooser(target,"WhatsApp hesabını seç"));
      else startActivity(target);
    } catch (ActivityNotFoundException | SecurityException err) {
      Toast.makeText(this,"Bu uygulama açılamadı. Uygulama seçicisini dene.",Toast.LENGTH_LONG).show();
    }
  }

  private String normalize(String s) {
    String digits=s.replaceAll("[^0-9]","");
    if (digits.matches("05[0-9]{9}")) digits="90"+digits.substring(1);
    else if (digits.matches("5[0-9]{9}")) digits="90"+digits;
    return digits.matches("[1-9][0-9]{9,14}")?digits:null;
  }
  private TextView text(String label,int size,int color,boolean bold) {
    TextView t=new TextView(this);
    t.setText(label);t.setTextSize(size);t.setTextColor(color);
    if (bold) t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
    return t;
  }
  private EditText input(String hint,int type,boolean single) {
    EditText i=new EditText(this);
    i.setInputType(type);i.setSingleLine(single);i.setHint(hint);
    i.setTextSize(15);i.setTextColor(0xff25344a);
    i.setPadding(dp(13),dp(13),dp(13),dp(13));i.setBackground(bg(Color.WHITE));
    return i;
  }
  private Button button(String title,boolean primary) {
    Button b=new Button(this);
    b.setText(title);b.setAllCaps(false);b.setTextSize(15);
    b.setTextColor(primary?Color.WHITE:0xff25344a);
    b.setBackground(bg(primary?0xffef633c:0xffe6ebf0));
    return b;
  }
  private GradientDrawable bg(int color) {
    GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(12));return g;
  }
  private int dp(int value) { return (int)(value*getResources().getDisplayMetrics().density+0.5f); }
  private void gap(LinearLayout root,int height) { root.addView(new View(this),new LinearLayout.LayoutParams(1,dp(height))); }
}

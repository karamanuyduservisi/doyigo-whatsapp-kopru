package com.doyigo.whatsappbridge;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayDeque;
import java.util.Locale;

/** One explicitly initiated test, one click. Nothing is sent if UI checks fail. */
public final class AutoSendService extends AccessibilityService {
  private long lastClick = 0L;
  @Override public void onAccessibilityEvent(AccessibilityEvent event) {
    if (event == null || event.getPackageName() == null) return;
    String pkg=event.getPackageName().toString();
    if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
    SharedPreferences p=getSharedPreferences("dg_wa_bridge",MODE_PRIVATE);
    if (p.getBoolean("pending_done",false)) return;
    String expected=p.getString("pending_msg","");
    String target=p.getString("pending_pkg","");
    long started=p.getLong("pending_started",0L),now=System.currentTimeMillis();
    if (expected == null || expected.isEmpty() || started == 0L) return;
    if (now-started>35000L || now<started-3000L) { clear(p);return; }
    if (target != null && !target.isEmpty() && !target.equals(pkg)) return;
    if (now-lastClick<500L) return;
    AccessibilityNodeInfo root=getRootInActiveWindow();
    if (root == null || root.getPackageName() == null ||
        !pkg.contentEquals(root.getPackageName())) return;
    ArrayDeque<AccessibilityNodeInfo> nodes=new ArrayDeque<>();
    nodes.add(root);
    boolean textMatches=false;
    AccessibilityNodeInfo send=null;
    int n=0;
    while (!nodes.isEmpty() && n++<450) {
      AccessibilityNodeInfo node=nodes.removeFirst();
      if (!node.isVisibleToUser()) continue;
      CharSequence val=node.getText();
      if (node.isEnabled() && node.isEditable() && val!=null &&
          expected.equals(val.toString().trim())) textMatches=true;
      if (send==null && node.isEnabled() && isSend(node)) {
        AccessibilityNodeInfo candidate=node;
        for (int i=0;i<3 && candidate!=null && !candidate.isClickable();i++)
          candidate=candidate.getParent();
        if (candidate!=null && candidate.isEnabled() && candidate.isClickable())
          send=candidate;
      }
      for (int i=0;i<Math.min(60,node.getChildCount());i++) {
        AccessibilityNodeInfo child=node.getChild(i);
        if (child!=null) nodes.addLast(child);
      }
    }
    if (!textMatches || send==null) return;
    // Stop future clicks BEFORE attempting send: never send twice.
    boolean recorded=p.edit().putBoolean("pending_done",true)
        .remove("pending_msg").remove("pending_pkg").remove("pending_started").commit();
    if (!recorded) return;
    lastClick=now;
    send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
  }
  private boolean isSend(AccessibilityNodeInfo n) {
    String id=n.getViewIdResourceName();
    if (id!=null && (id.endsWith(":id/send") || id.endsWith(":id/send_button") ||
        id.endsWith(":id/compose_send"))) return true;
    CharSequence desc=n.getContentDescription();
    if (desc==null) return false;
    String d=desc.toString().toLowerCase(Locale.ROOT).trim();
    return d.equals("gönder") || d.equals("send") ||
        d.equals("mesajı gönder") || d.equals("send message");
  }
  private void clear(SharedPreferences p) {
    p.edit().remove("pending_msg").remove("pending_pkg")
      .remove("pending_started").remove("pending_done").apply();
  }
  @Override public void onInterrupt() {}
}

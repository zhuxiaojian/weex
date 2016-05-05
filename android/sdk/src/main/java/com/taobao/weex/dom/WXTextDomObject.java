/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.taobao.weex.dom;

import android.graphics.Typeface;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.common.WXDomPropConstant;
import com.taobao.weex.dom.flex.CSSConstants;
import com.taobao.weex.dom.flex.CSSNode;
import com.taobao.weex.dom.flex.MeasureOutput;
import com.taobao.weex.ui.component.WXText;
import com.taobao.weex.ui.component.WXTextDecoration;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for calculating a given text's height and width. The calculating of width and height of
 * text is done by {@link Layout}.
 */
public class WXTextDomObject extends WXDomObject {

  private static class SetSpanOperation {

    protected int start, end;
    protected Object what;

    SetSpanOperation(int start, int end, Object what) {
      this.start = start;
      this.end = end;
      this.what = what;
    }

    public void execute(SpannableStringBuilder sb) {
      int spanFlags = Spannable.SPAN_EXCLUSIVE_INCLUSIVE;
      if (start == 0) {
        spanFlags = Spannable.SPAN_INCLUSIVE_INCLUSIVE;
      }
      sb.setSpan(what, start, end, spanFlags);
    }
  }


  /**
   * Object for calculating text's width and height. This class is an anonymous class of
   * implementing {@link com.taobao.weex.dom.flex.CSSNode.MeasureFunction}
   */
  private static final CSSNode.MeasureFunction TEXT_MEASURE_FUNCTION = new CSSNode.MeasureFunction() {
    @Override
    public void measure(CSSNode node, float width, MeasureOutput measureOutput) {
      WXTextDomObject textDomObject = (WXTextDomObject) node;
      Spanned text = textDomObject.mPreparedSpannedText;
      if (text == null) {
        return;
      }

      if (CSSConstants.isUndefined(width)) {
        width = node.cssstyle.maxWidth;
      }

      Layout layout = textDomObject.createLayoutFromSpan(width, sTextPaintInstance, text);
      measureOutput.height = layout.getHeight();
      measureOutput.width = layout.getWidth();
      textDomObject.layout = layout;
    }
  };

  public static final int UNSET = -1;
  private static final TextPaint sTextPaintInstance = new TextPaint();
  public Layout layout;
  public Spanned mPreparedSpannedText;
  protected int mNumberOfLines = UNSET;
  protected int mFontSize = UNSET;
  protected Layout.Alignment mAlignment;
  private boolean mIsColorSet = false;
  private int mColor;
  /**
   * mFontStyle can be {@link Typeface#NORMAL} or {@link Typeface#ITALIC}.
   * mFontWeight can be {@link Typeface#NORMAL} or {@link Typeface#BOLD}.
   */
  private int mFontStyle = UNSET;
  private int mFontWeight = UNSET;
  private String mFontFamily = null;
  private String mText = null;
  private WXTextDecoration mTextDecoration = WXTextDecoration.NONE;

  static {
    sTextPaintInstance.setFlags(TextPaint.ANTI_ALIAS_FLAG);
  }

  /**
   * Create an instance of current class, and set {@link #TEXT_MEASURE_FUNCTION} as the
   * measureFunction
   * @see CSSNode#setMeasureFunction(MeasureFunction)
   */
  public WXTextDomObject() {
    super();
    setMeasureFunction(TEXT_MEASURE_FUNCTION);
  }

  private Layout createLayoutFromSpan(float width, TextPaint textPaint, Spanned text) {
    Layout layout;
    BoringLayout.Metrics boring = BoringLayout.isBoring(text, textPaint);
    if (boring != null
        && (CSSConstants.isUndefined(width) || boring.width <= width)) {
      layout = BoringLayout.make(text, textPaint, (int) width,
                                 Layout.Alignment.ALIGN_NORMAL, 1, 0, boring, false);
    } else {
      layout = new StaticLayout(text, textPaint, (int) width,
                                Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
    }
    if (mNumberOfLines != UNSET && mNumberOfLines < layout.getLineCount()) {
      int textEnd = layout.getLineEnd(mNumberOfLines-1);
      textEnd=textEnd+1<layout.getText().length()?textEnd+1:textEnd;
      layout = new StaticLayout(text, 0, textEnd, textPaint, (int) width,
                                Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
    }
    return layout;
  }

  /**
   * Prepare the text {@link Spanned} for calculating text's size. This is done by setting
   * various text span to the text.
   * @see android.text.style.CharacterStyle
   */
  @Override
  public void layoutBefore() {
    initData();
    mPreparedSpannedText = fromTextCSSNode(this);
    super.dirty();
    super.layoutBefore();
  }

  @Override
  public void layoutAfter() {
    if (layout == null) {
      layout = createLayoutFromSpan(getLayoutWidth(), sTextPaintInstance, mPreparedSpannedText);
    }
    super.layoutAfter();
  }

  @Override
  public Object getExtra() {
    return mPreparedSpannedText;
  }

  @Override
  public void updateAttr(Map<String, Object> attrs) {
    super.updateAttr(attrs);
    if (attrs.containsKey(WXDomPropConstant.WX_ATTR_VALUE)) {
      mText = WXAttr.getValue(attrs);
    }
  }

  @Override
  public void updateStyle(Map<String, Object> styles) {
    super.updateStyle(styles);
    update(styles);
  }

  @Override
  public WXTextDomObject clone() {
    WXTextDomObject dom = null;
    try {
      dom = new WXTextDomObject();
      if (this.cssstyle != null) {
        dom.cssstyle.copy(this.cssstyle);
      }

      dom.ref = ref;
      dom.type = type;
      dom.style = style;
      dom.attr = attr;
      dom.event = event == null ? null : event.clone();
      dom.layout = layout;
      if (this.csslayout != null) {
        dom.csslayout.copy(this.csslayout);
      }
    } catch (Exception e) {
      if (WXEnvironment.isApkDebugable()) {
        WXLogUtils.e("WXTextDomObject clone error: " + WXLogUtils.getStackTrace(e));
      }
    }
    if (dom != null) {
      dom.mPreparedSpannedText = mPreparedSpannedText;
    }
    return dom;
  }

  private void initData() {
    update(style);
    if (attr != null) {
      mText = WXAttr.getValue(attr);
    }
  }

  protected static Spanned fromTextCSSNode(WXTextDomObject textCSSNode) {
    SpannableStringBuilder sb = new SpannableStringBuilder();
    List<SetSpanOperation> ops = new ArrayList<>();
    buildSpannedFromTextCSSNode(textCSSNode, sb, ops);
    if (textCSSNode.mFontSize == UNSET) {
      sb.setSpan(
          new AbsoluteSizeSpan(WXText.sDEFAULT_SIZE), 0, sb
              .length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }
    for (int i = ops.size() - 1; i >= 0; i--) {
      SetSpanOperation op = ops.get(i);
      op.execute(sb);
    }
    return sb;
  }

  private void update(Map<String, Object> style) {
    if (style != null) {
      if (style.containsKey(WXDomPropConstant.WX_LINES)) {
        int lines = WXStyle.getLines(style);
        if (lines > 0) {
          mNumberOfLines = lines;
        }
      }
      if (style.containsKey(WXDomPropConstant.WX_FONTSIZE)) {
        mFontSize = WXStyle.getFontSize(style);
      }
      if (style.containsKey(WXDomPropConstant.WX_FONTWEIGHT)) {
        mFontWeight = WXStyle.getFontWeight(style);
      }
      if (style.containsKey(WXDomPropConstant.WX_FONTSTYLE)) {
        mFontStyle = WXStyle.getFontStyle(style);
      }
      if (style.containsKey(WXDomPropConstant.WX_COLOR)) {
        mColor = WXResourceUtils.getColor(WXStyle.getTextColor(style));
        mIsColorSet = mColor != Integer.MIN_VALUE;
      }
      if (style.containsKey(WXDomPropConstant.WX_TEXTDECORATION)) {
        mTextDecoration = WXStyle.getTextDecoration(style);
      }
      if (style.containsKey(WXDomPropConstant.WX_FONTFAMILY)) {
        mFontFamily = WXStyle.getFontFamily(style);
      }
      mAlignment = WXStyle.getTextAlignment(style);
    }
  }

  private static void buildSpannedFromTextCSSNode(
      WXTextDomObject textCSSNode, SpannableStringBuilder sb,
      List<SetSpanOperation> ops) {
    int start = 0;
    if (textCSSNode.mText != null) {
      sb.append(textCSSNode.mText);
    }
    int end = sb.length();
    if (end >= start) {
      if (textCSSNode.mTextDecoration == WXTextDecoration.UNDERLINE) {
        ops.add(new SetSpanOperation(start, end,
                                     new UnderlineSpan()));
      }
      if (textCSSNode.mTextDecoration == WXTextDecoration.LINETHROUGH) {
        ops.add(new SetSpanOperation(start, end,
                                     new StrikethroughSpan()));
      }
      if (textCSSNode.mIsColorSet) {
        ops.add(new SetSpanOperation(start, end,
                                     new ForegroundColorSpan(textCSSNode.mColor)));
      }
      if (textCSSNode.mFontSize != UNSET) {
        ops.add(new SetSpanOperation(start, end, new AbsoluteSizeSpan(
            textCSSNode.mFontSize)));
      }
      if (textCSSNode.mFontStyle != UNSET
          || textCSSNode.mFontWeight != UNSET
          || textCSSNode.mFontFamily != null) {
        ops.add(new SetSpanOperation(start, end, new WXCustomStyleSpan(
            textCSSNode.mFontStyle, textCSSNode.mFontWeight,
            textCSSNode.mFontFamily)));
      }
      ops.add(new SetSpanOperation(start, end, new AlignmentSpan.Standard(textCSSNode.mAlignment)));
    }
  }

}

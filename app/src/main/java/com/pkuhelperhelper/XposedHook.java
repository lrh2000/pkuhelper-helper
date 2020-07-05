package com.pkuhelperhelper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHook implements IXposedHookLoadPackage {

    static private ConfigObserver configUpdater = null;

    static private ClipboardManager clipboardManager = null;

    static private ImageView pendingImageView = null;
    static private Bitmap availableBitmap = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.pkuhelper.beta")) {
            new XC_MethodHook() {
                protected XC_MethodHook.Unhook unhooker;

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    unhooker.unhook();

                    Activity activity = (Activity) param.args[0];
                    configUpdater = new ConfigObserver(activity.getExternalFilesDir(null));
                    clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                }

                public void hook() {
                    unhooker = XposedHelpers.findAndHookMethod("android.app.Instrumentation",
                            lpparam.classLoader, "callActivityOnCreate",
                            Activity.class, Bundle.class, this);
                }
            }.hook();

            XposedHelpers.findAndHookMethod("com.pkuhelper.hole.detail.HoleDetailFragment",
                    lpparam.classLoader, "onCreateView",
                    LayoutInflater.class, ViewGroup.class, Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object header = XposedHelpers.getObjectField(param.thisObject, "mHeaderViewHolder");
                            View view = (View) XposedHelpers.getObjectField(header, "mContainer");
                            final TextView textView = (TextView) XposedHelpers.getObjectField(header, "mTextContent");

                            view.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    if (!ConfigManager.getLongClickToCopy())
                                        return false;
                                    ClipData clipData = ClipData.newPlainText("hole detail", textView.getText());
                                    clipboardManager.setPrimaryClip(clipData);
                                    Toast.makeText(textView.getContext(), "The text is copied.", Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                            });
                        }
                    });

            Class<?> R_id = lpparam.classLoader.loadClass("com.pkuhelper.R$id");
            final int id_card = XposedHelpers.getStaticIntField(R_id, "card");

            XposedHelpers.findAndHookMethod("com.pkuhelper.hole.detail.HoleCommentListAdapter",
                    lpparam.classLoader, "convert",
                    lpparam.classLoader.loadClass("com.chad.library.adapter.base.BaseViewHolder"),
                    lpparam.classLoader.loadClass("com.pkuhelper.data.hole.HoleComment"),
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            final String content = (String) XposedHelpers.callMethod(param.args[1], "getText");
                            final Context context = (Context) XposedHelpers.getObjectField(
                                    XposedHelpers.getObjectField(param.thisObject, "mTimeManager"), "mContext"
                            );

                            XposedHelpers.callMethod(param.args[0], "setOnLongClickListener",
                                    id_card, new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {
                                            if (!ConfigManager.getLongClickToCopy())
                                                return false;
                                            ClipData clipData = ClipData.newPlainText("hole detail", content);
                                            clipboardManager.setPrimaryClip(clipData);
                                            Toast.makeText(context, "The text is copied.", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    });
                        }
                    });

            XposedHelpers.findAndHookMethod("com.pkuhelper.hole.HoleListAdapter",
                    lpparam.classLoader, "convert",
                    lpparam.classLoader.loadClass("com.chad.library.adapter.base.BaseViewHolder"),
                    lpparam.classLoader.loadClass("com.pkuhelper.data.hole.Hole"),
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            final String content = (String) XposedHelpers.callMethod(param.args[1], "getText");
                            final Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                            XposedHelpers.callMethod(param.args[0],"setOnLongClickListener",
                                    id_card, new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {
                                            if (!ConfigManager.getLongClickToCopy())
                                                return false;
                                            ClipData clipData = ClipData.newPlainText("hole detail", content);
                                            clipboardManager.setPrimaryClip(clipData);
                                            Toast.makeText(context, "The text is copied.", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    });
                        }
                    });

            XposedHelpers.findAndHookMethod("com.pkuhelper.hole.HoleListAdapter",
                    lpparam.classLoader, "lambda$convert$0",
                    lpparam.classLoader.loadClass("com.pkuhelper.data.hole.Hole"), View.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            Object builder = XposedHelpers.newInstance(
                                    lpparam.classLoader.loadClass("com.pkuhelper.container.image.ImagePreviewContainer$Builder"),
                                    XposedHelpers.getObjectField(param.thisObject, "mContext")
                            );
                            String url = (String) XposedHelpers.callMethod(param.args[0], "getResourceUrl");
                            url += "#";
                            url += Integer.valueOf(XposedHelpers.getIntField(param.args[0], "pid")).toString();
                            XposedHelpers.callMethod(builder, "url", url);
                            XposedHelpers.callMethod(builder, "show");
                            return null;
                        }
                    });

            XposedHelpers.findAndHookMethod("com.pkuhelper.hole.detail.HoleDetailFragment",
                    lpparam.classLoader, "lambda$setHeader$1",
                    lpparam.classLoader.loadClass("com.pkuhelper.data.hole.Hole"), View.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            Object builder = XposedHelpers.newInstance(
                                    lpparam.classLoader.loadClass("com.pkuhelper.container.image.ImagePreviewContainer$Builder"),
                                    XposedHelpers.callMethod(param.thisObject, "getContext")
                            );
                            String url = (String) XposedHelpers.callMethod(param.args[0], "getResourceUrl");
                            url += "#";
                            url += Integer.valueOf(XposedHelpers.getIntField(param.args[0], "pid")).toString();
                            XposedHelpers.callMethod(builder,"url", url);
                            XposedHelpers.callMethod(builder, "show");
                            return null;
                        }
                    });

            XposedHelpers.findAndHookMethod("uk.co.senab.photoview.PhotoViewAttacher",
                    lpparam.classLoader, "onTouch",
                    View.class, MotionEvent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(false);
                        }
                    });

            XposedHelpers.findAndHookMethod("com.squareup.picasso.PicassoDrawable",
                    lpparam.classLoader, "setBitmap",
                    ImageView.class, Context.class, Bitmap.class,
                    lpparam.classLoader.loadClass("com.squareup.picasso.Picasso$LoadedFrom"),
                    boolean.class, boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (pendingImageView == param.args[0]) {
                                pendingImageView = null;
                                availableBitmap = (Bitmap) param.args[2];
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod("com.pkuhelper.container.image.ImagePreviewActivity",
                    lpparam.classLoader, "setupContent",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Intent intent = (Intent) XposedHelpers.callMethod(param.thisObject, "getIntent");
                            String data = intent.getStringExtra("KEY_URL");
                            int idx = data.lastIndexOf('#');
                            String url = data.substring(0, idx);
                            final String pid = data.substring(idx + 1);
                            intent.putExtra("KEY_URL", url);

                            final Context context = (Context) param.thisObject;
                            final ImageView imageView = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mPreview");
                            imageView.setLongClickable(true);
                            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    if (!ConfigManager.getLongClickToSavePicture())
                                        return false;
                                    Bitmap bitmap = availableBitmap;
                                    if (bitmap == null) {
                                        Toast.makeText(context, "The picture is not ready now.", Toast.LENGTH_LONG).show();
                                        return true;
                                    }

                                    String path = Environment.DIRECTORY_PICTURES + File.separator + "pkuhole";
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "hole" + pid);
                                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path);

                                    ContentResolver resolver = context.getContentResolver();
                                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                    uri = resolver.insert(uri, contentValues);

                                    try {
                                        OutputStream os = resolver.openOutputStream(uri);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                                        os.close();

                                        Toast.makeText(context, "The picture is saved to the gallery.", Toast.LENGTH_SHORT).show();
                                        return true;
                                    } catch (IOException e) {
                                        Toast.makeText(context, "Failed to save the picture. (" +
                                                e.getMessage() + ")", Toast.LENGTH_LONG).show();
                                        XposedBridge.log("Failed to save the picture");
                                        XposedBridge.log(e);
                                        return true;
                                    }
                                }
                            });

                            pendingImageView = imageView;
                            availableBitmap = null;
                        }
                    });
        }
    }
}

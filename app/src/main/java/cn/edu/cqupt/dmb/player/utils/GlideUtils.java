package cn.edu.cqupt.dmb.player.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.util.concurrent.ExecutionException;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 图片加载工具
 * @Date : create by QingSong in 2022-05-29 14:43
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class GlideUtils {

    private static final String TAG = "GlideUtils";

    public static Bitmap loadBitMap(Context context, byte[] fileBuffer) {

        Bitmap bitmap = null;

        FutureTarget<Bitmap> futureTarget = Glide.with(context).asBitmap().load(fileBuffer).centerCrop().submit();

        try {
            bitmap = futureTarget.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "loadBitMap: " + "获取 bitmap 失败");
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap loadBitMap(Context context, String path) {

        Bitmap bitmap = null;

        FutureTarget<Bitmap> futureTarget = Glide.with(context).asBitmap().load(path).centerCrop().submit();

        try {
            bitmap = futureTarget.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "loadBitMap: " + "获取 bitmap 失败");
            e.printStackTrace();
        }

        return bitmap;
    }


//    /**
//     * @param path        文件路径-绝对路径
//     * @param maxSize     读入图片的最大面积（一般1024*1024就可以了）
//     * @param fixRotation
//     * @return 图片bitmap
//     */
//    public static Bitmap decodeFileLimit(String path, int maxSize, boolean fixRotation) {
//        if (TextUtils.isEmpty(path) || !AndroidQCompat.isFileExist(path)) {
//            Log.e(TAG, "decodeFileLimit: 文件不存在 path->" + path);
//            return null;
//        }
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        if (AndroidQCompat.isContentUriString(path)) {
//            ContentResolver contentResolver = VAV.context.getContentResolver();
//            Uri uri = Uri.parse(path);
//            try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")) {
//                if (parcelFileDescriptor != null) {
//                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//                    BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
//                } else {
//                    return null;
//                }
//            } catch (FileNotFoundException e) {
//                Log.e(TAG, "decodeFileLimit: ", e);
//                return null;
//            } catch (IOException ee) {
//                Log.e(TAG, "decodeFileLimit: ", ee);
//                return null;
//            }
//        } else {
//            BitmapFactory.decodeFile(path, options);
//        }
//        options.inJustDecodeBounds = false;
//
//        // 采样率
//        Camera.Size size = M.calcSize(maxSize, options.outWidth * 1f / options.outHeight);
//        options.inSampleSize = calculateInSampleSize(options, size.width, size.height);
////        Log.e(TAG, "decodeFileLimit: w->" + options.outWidth + " h->" + options.outHeight
////                + " inSampleSize->" + options.inSampleSize);
//        int sampleWidth = options.outWidth / options.inSampleSize;
//        int sampleHeight = options.outHeight / options.inSampleSize;
//        int sampleSize = sampleWidth * sampleHeight;
//        double scale = 1.0;
//        if (maxSize > 0 && sampleSize > maxSize) {
//            scale = (float) Math.sqrt(maxSize * 1.0 / sampleSize);
//        }
//        options.inDensity = 1000000;    // 大小会影响精度。。。
//        options.inTargetDensity = (int) (options.inDensity * scale);
//
//        Bitmap bitmap;
//        if (AndroidQCompat.isContentUriString(path)) {
//            Uri uri = Uri.parse(path);
//            ContentResolver contentResolver = VAV.context.getContentResolver();
//            try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")) {
//                if (parcelFileDescriptor != null) {
//                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
//                } else {
//                    Log.e(TAG, "decodeFileLimit: parcelFileDescriptor null");
//                    return null;
//                }
//            } catch (Exception e) {
//                if (BuildConfig.DEBUG) {
//                    throw new RuntimeException(e);
//                }
//                Log.e(TAG, "decodeFileLimit: ", e);
//                return null;
//            }
//        } else {
//            bitmap = BitmapFactory.decodeFile(path, options);
//        }
//        if (bitmap == null) {
//            Log.e(TAG, "decodeFileLimit: decode failed.???" + path + " " + maxSize + " " + scale);
//            return null;
//        }
////        Log.e(TAG, "decodeFileLimit: " + bitmap.getWidth() + "x" + bitmap.getHeight() +
////                "=" + (bitmap.getWidth() * bitmap.getHeight()));
////        Log.e(TAG, "decodeFileLimit: maxSize->" + maxSize + " scale->" + scale);
//        bitmap.setDensity(TypedValue.DENSITY_DEFAULT);
//        if (fixRotation) {
//            try (final FileInputStream fis = new FileInputStream(new File(path))) {
//                int orientation = readPictureOrientation(fis.getFD());
//                bitmap = rotateBitmapByOrientation(bitmap, orientation);
//            } catch (Exception e) {
//                Log.e(TAG, "decodeFileLimit: ", e);
//                if (BuildConfig.DEBUG) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        bitmap = convertToARGB8IfNot(bitmap);
//        return bitmap;
//    }
//
//    private static int readPictureOrientation(FileDescriptor fd) {
//        int orientation = ExifInterface.ORIENTATION_NORMAL;
//        try {
//            ExifInterface exifInterface = new ExifInterface(fd);
//            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//        } catch (IOException | Error e) {
//            e.printStackTrace();
//        }
//        return orientation;
//    }
//
//    /*
//     * 根据图片Exif中的orientation来旋转图片
//     * @param bitmap
//     * @param orientation
//     * @return
//     */
//    public static Bitmap rotateBitmapByOrientation(Bitmap bitmap, int orientation) {
//        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
//            return bitmap;
//        }
//
//        Bitmap resultBm = null;
//        Matrix matrix = new Matrix();
//        switch (orientation) {
//            case ExifInterface.ORIENTATION_ROTATE_90: {
//                matrix.postRotate(90);
//                break;
//            }
//            case ExifInterface.ORIENTATION_TRANSPOSE: {
//                matrix.postRotate(90);
//                matrix.postScale(-1, 1);
//                break;
//            }
//            case ExifInterface.ORIENTATION_ROTATE_180: {
//                matrix.postRotate(180);
//                break;
//            }
//            case ExifInterface.ORIENTATION_FLIP_VERTICAL: {
//                matrix.postRotate(180);
//                matrix.postScale(-1, 1);
//                break;
//            }
//            case ExifInterface.ORIENTATION_ROTATE_270: {
//                matrix.postRotate(270);
//                break;
//            }
//            case ExifInterface.ORIENTATION_TRANSVERSE: {
//                matrix.setRotate(270);
//                matrix.postScale(-1, 1);
//                break;
//            }
//            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: {
//                matrix.setScale(-1, 1);
//                break;
//            }
//        }
//        try {
//            resultBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
//        }
//        if (resultBm == null) {
//            resultBm = bitmap;
//        }
//        if (bitmap != resultBm) {
//            bitmap.recycle();
//        }
//        return resultBm;
//    }
//
//    public static Bitmap convertToARGB8IfNot(Bitmap bitmap) {
//        if (bitmap == null) {
//            return null;
//        }
//        final Bitmap.Config config = bitmap.getConfig();
//        if (!TextUtils.equals(config.name(), Bitmap.Config.ARGB_8888.name())) {
//            Bitmap convert = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            if (convert != bitmap) {
//                recycleBm(bitmap);
//            }
//            return convert;
//        }
//        return bitmap;
//    }
//
//    public static void recycleBm(Bitmap bitmap) {
//        if (bitmap == null) {
//            return;
//        }
//        if (!bitmap.isRecycled()) {
//            bitmap.recycle();
//            System.gc();
//        }
//    }
}

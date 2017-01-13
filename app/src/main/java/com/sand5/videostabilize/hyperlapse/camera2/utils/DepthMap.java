package com.sand5.videostabilize.hyperlapse.camera2.utils;

import java.io.File;
import java.io.FileInputStream;

class DepthMap {
    private static final String[] NAMES = {
            "00", "01", "02", "03", "04", "AllFocusImage"
    };

    private byte[] mData;
    private int mWidth;
    private int mHeight;
    private boolean mFail = true;

    public DepthMap(final String path) {
        File file = new File(path);
        try {
            FileInputStream stream = new FileInputStream(file);
            mData = new byte[(int) file.length()];
            stream.read(mData);
            stream.close();
        } catch (Exception e) {
        }

        int length = mData.length;
        if (length > 25) {
            mFail = (mData[length - 25] != 0);
            mWidth = readInteger(length - 24);
            mHeight = readInteger(length - 20);
        }
        if (mWidth * mHeight + 25 > length) {
            mFail = true;
        }
    }

    public int getDepth(float x, float y) {
        if (mFail || x > 1.0f || y > 1.0f) {
            return NAMES.length - 1;
        } else {
            return mData[(int) ((y * mHeight + x) * mWidth)];
        }
    }

    private int readInteger(int offset) {
        int result = mData[offset] & 0xff;
        for (int i = 1; i < 4; ++i) {
            result <<= 8;
            result += mData[offset + i] & 0xff;
        }
        return result;
    }
}
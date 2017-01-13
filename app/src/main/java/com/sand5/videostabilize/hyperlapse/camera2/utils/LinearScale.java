package com.sand5.videostabilize.hyperlapse.camera2.utils;

/**
 * Represents a discrete linear scale function.
 */
public final class LinearScale {
    private final float mDomainA;
    private final float mDomainB;
    private final float mRangeA;
    private final float mRangeB;

    private final float mScale;

    public LinearScale(float domainA, float domainB, float rangeA, float rangeB) {
        mDomainA = domainA;
        mDomainB = domainB;
        mRangeA = rangeA;
        mRangeB = rangeB;

        // Precomputed ratio between input domain and output range.
        float scale = (mRangeB - mRangeA) / (mDomainB - mDomainA);
        mScale = Float.isNaN(scale) ? 0.0f : scale;
    }

    /**
     * Clamp a given domain value to the given domain.
     */
    public float clamp(float domainValue) {
        if (mDomainA > mDomainB) {
            return Math.max(mDomainB, Math.min(mDomainA, domainValue));
        }

        return Math.max(mDomainA, Math.min(mDomainB, domainValue));
    }

    /**
     * Returns true if the value is within the domain.
     */
    public boolean isInDomain(float domainValue) {
        if (mDomainA > mDomainB) {
            return domainValue <= mDomainA && domainValue >= mDomainB;
        }
        return domainValue >= mDomainA && domainValue <= mDomainB;
    }

    /**
     * Linearly scale a given domain value into the output range.
     */
    public float scale(float domainValue) {
        return mRangeA + (domainValue - mDomainA) * mScale;
    }

    /**
     * For the current domain and range parameters produce a new scale function
     * that is the inverse of the current scale function.
     */
    public LinearScale inverse() {
        return new LinearScale(mRangeA, mRangeB, mDomainA, mDomainB);
    }

    @Override
    public String toString() {
        return "LinearScale{" +
                "mDomainA=" + mDomainA +
                ", mDomainB=" + mDomainB +
                ", mRangeA=" + mRangeA +
                ", mRangeB=" + mRangeB + "}";
    }
}
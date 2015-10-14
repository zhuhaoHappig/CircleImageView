import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView {

	private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

	private static final int DEFAULT_BORDER_WIDTH = 0;
	private static final int DEFAULT_BORDER_COLOR = Color.TRANSPARENT;

	private final RectF mDrawableRect = new RectF();
	private final RectF mBorderRect = new RectF();

	private final Matrix mShaderMatrix = new Matrix();
	private final Paint mBitmapPaint = new Paint();
	private final Paint mBorderPaint = new Paint();

	private int mBorderColor = DEFAULT_BORDER_COLOR;
	private int mBorderWidth = DEFAULT_BORDER_WIDTH;

	private Bitmap mBitmap;
	private BitmapShader mBitmapShader;
	private int mBitmapWidth;
	private int mBitmapHeight;

	private float mDrawableRadius;
	private float mBorderRadius;

	private int width;

	private int height;

	public CircleImageView(Context context) {
		this(context,null);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setScaleType(SCALE_TYPE);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.CircleImageView, defStyle, 0);

		mBorderWidth = a.getDimensionPixelSize(
				R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
		mBorderColor = a.getColor(R.styleable.CircleImageView_border_color,
				DEFAULT_BORDER_COLOR);

		width = a.getDimensionPixelSize(R.styleable.CircleImageView_width_VIEW,
				0);
		height = a.getDimensionPixelSize(
				R.styleable.CircleImageView_height_VIEW, 0);

		a.recycle();
		
		setBitmap();
		
	}

	@Override
	public ScaleType getScaleType() {
		return SCALE_TYPE;
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		if (scaleType != SCALE_TYPE) {
			throw new IllegalArgumentException(String.format(
					"ScaleType %s not supported.", scaleType));
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		setBitmap();
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		setBitmap();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		setBitmap();

	}
	
	private void setBitmap(){
		mBitmap = getBitmapFromDrawable(getDrawable());
		setup();
	}
	
	private boolean noSize(){
		if(width == 0 || height == 0){
			return true;
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(noSize()){
			super.onDraw(canvas);
		}else{
			if (getDrawable() == null) {
				return;
			}
			
			canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius,
			                  mBitmapPaint);
			canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius,
			                  mBorderPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setup();
	}

	public int getBorderColor() {
		return mBorderColor;
	}

	public void setBorderColor(int borderColor) {
		if (borderColor == mBorderColor) {
			return;
		}

		mBorderColor = borderColor;
		mBorderPaint.setColor(mBorderColor);
		invalidate();
	}

	public int getBorderWidth() {
		return mBorderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		if (borderWidth == mBorderWidth) {
			return;
		}

		mBorderWidth = borderWidth;
		setup();
	}

	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		if(drawable instanceof NinePatchDrawable){
			return null;
		}
		if (noSize()) {
			return null;
		}
		try {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if(bitmap != null){
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				                    bitmap.getHeight(), 
				                    getMatrix(drawable.getIntrinsicWidth(), 
				                    drawable.getIntrinsicHeight()), true);
				
				return bitmap;
			}else{
				return null;
			}
		} catch (OutOfMemoryError e) {
			return null;
		}
	}
	
	private Matrix getMatrix(int DWidth,int DHeight){
		Matrix matrix = new Matrix();
		float scale = DWidth * 1.0f
				/ DHeight;
		float dw = height * scale;
		float ScaleX = dw * 1.0f / DWidth;
		float ScaleY = height * 1.0f / DHeight;
		matrix.setScale(ScaleX, ScaleY);
		return matrix;
	}

	private void setup() {
		
		if (mBitmap == null) {
			return;
		}

		mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP);

		mBitmapPaint.setAntiAlias(true);
		mBitmapPaint.setShader(mBitmapShader);

		mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);

		mBitmapHeight = mBitmap.getHeight();
		mBitmapWidth = mBitmap.getWidth();

		mBorderRect.set(0, 0, getWidth(), getHeight());
		mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2,
				(mBorderRect.width() - mBorderWidth) / 2);

		mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width()
				- mBorderWidth, mBorderRect.height() - mBorderWidth);
		mDrawableRadius = Math.min(mDrawableRect.height() / 2,
				mDrawableRect.width() / 2);
		updateShaderMatrix();
		invalidate();
	}

	private void updateShaderMatrix() {
		float scale;
		float dx = 0;
		float dy = 0;

		mShaderMatrix.set(null);

		if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width()
				* mBitmapHeight) {
			scale = mDrawableRect.height() / (float) mBitmapHeight;
			dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
		} else {
			scale = mDrawableRect.width() / (float) mBitmapWidth;
			dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
		}

		mShaderMatrix.setScale(scale, scale);
		mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth,
				(int) (dy + 0.5f) + mBorderWidth);

		mBitmapShader.setLocalMatrix(mShaderMatrix);
	}

}

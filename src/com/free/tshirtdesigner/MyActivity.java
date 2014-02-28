package com.free.tshirtdesigner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.free.tshirtdesigner.action.InputActionListener;
import com.free.tshirtdesigner.action.TextChangeListener;
import com.free.tshirtdesigner.adapter.LayerArrayAdapter;
import com.free.tshirtdesigner.dialog.InputDialog;
import com.free.tshirtdesigner.model.LayerModel;
import com.free.tshirtdesigner.util.UtilImage;
import com.free.tshirtdesigner.util.setting.ConstantValue;

import java.io.File;
import java.util.*;

import static android.widget.RelativeLayout.LayoutParams;

public class MyActivity extends FragmentActivity
{
    boolean exist = false;
    private static final int CHECKOUT_CODE = 100;
    private int home_x, home_y;
    private LayoutParams layoutParams;
    private Button btTakePhoto;
    private Button btAddImage;
    private RelativeLayout shapeLayout;
    private ImageView ivImageShow;
    private ImageView ivResizeBottom;
    private ImageView ivResizeTop;
    //    private RelativeLayout rlRootLayout;
    private Button btnCheckout;
    private Button btnLeftMenu;
    private Button btnRightMenu;
    private Button btAddText;

    private int tShirtDirection;
    private int _yDelta;
    private int _xDelta;
    public static final String FRONT_TAG = "FRONT";
    public static final String LEFT_TAG = "LEFT";
    public static final String RIGHT_TAG = "RIGHT";
    public static final String BACK_TAG = "BACK";


    TShirtFragment tShirtFragment;
    private int countLayer = -1;
    private ListView lvListLayer;
    private int currentLayer = -1;
    private List<LayerModel> layerModels = new ArrayList<LayerModel>();
    Map<String, List<View>> zoomViewsMap = new HashMap<String, List<View>>();
    List<View> currentZoomView = new ArrayList<View>();
    public String currentSide;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        currentSide = FRONT_TAG;
        createOrUpdateFragment(FRONT_TAG);

        setUpViewById();
        setActionListener();

        // footer controller
        RadioGroup rgShirtViewType = (RadioGroup) findViewById(R.id.rgShirtViewType);
        rgShirtViewType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (group.getCheckedRadioButtonId())
                {
                    case R.id.rbLeftSide:
                        createOrUpdateFragment(LEFT_TAG);
                        break;
                    case R.id.rbBackSide:
                        createOrUpdateFragment(BACK_TAG);
                        break;
                    case R.id.rbFrontSide:
                        createOrUpdateFragment(FRONT_TAG);
                        break;
                    case R.id.rbRightSide:
                        createOrUpdateFragment(RIGHT_TAG);
                        break;
                }
            }
        });

        RadioButton rbFront = (RadioButton) findViewById(R.id.rbFrontSide);
        rbFront.setChecked(true);
    }

    private void setUpViewById()
    {
        shapeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.shape_layout, null);
        btTakePhoto = (Button) findViewById(R.id.footer_control_btTakePhoto);
        ivImageShow = (ImageView) shapeLayout.findViewById(R.id.main_activity_ivImage);
        ivImageShow.setTag("ImageShow");
        ivResizeTop = (ImageView) shapeLayout.findViewById(R.id.main_activity_ivResizeTop);
        ivResizeTop.setTag("ResizeTop");
        ivResizeBottom = (ImageView) shapeLayout.findViewById(R.id.main_activity_ivResizeBottom);
        ivResizeBottom.setTag("ResizeBottom");

        btnCheckout = (Button) findViewById(R.id.header_btCheckout);

        btnRightMenu = (Button) findViewById(R.id.footer_control_btShowRightMenu);
        btnLeftMenu = (Button) findViewById(R.id.footer_control_btShowLeftMenu);
        btAddImage = (Button) findViewById(R.id.footer_control_btAddImage);
        btAddText = (Button) findViewById(R.id.footer_control_btAddText);
    }

    private void setActionListener()
    {
        btTakePhoto.setOnClickListener(onClickListener);
        btnCheckout.setOnClickListener(onClickListener);

        btnLeftMenu.setOnClickListener(onClickListener);
        btnRightMenu.setOnClickListener(onClickListener);
        btAddImage.setOnClickListener(onClickListener);
        btAddText.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.footer_control_btAddImage:
                    Intent intentGallery = new Intent();
                    intentGallery.setType("image/*");
                    intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intentGallery, "Complete action using"), ConstantValue.PICK_FROM_FILE);
                    break;
                case R.id.footer_control_btTakePhoto:
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, ConstantValue.CAPTURE_PICTURE);
                    break;
                case R.id.header_btCheckout:
                    Intent intent = new Intent(MyActivity.this, CheckoutActivity.class);
                    startActivityForResult(intent, CHECKOUT_CODE);
                    break;
                case R.id.footer_control_btShowLeftMenu:
                    showLeftMenu();
                    break;
                case R.id.footer_control_btShowRightMenu:
                    showRightMenu();
                    break;
                case R.id.footer_control_btAddText:
                    new InputDialog(new InputActionListener()
                    {
                        @Override
                        public void onSubmit(String result)
                        {
                            ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), result, null, null);
                            currentZoomView.add(viewZoomer);
                            tShirtFragment.getRlRootLayout().addView(viewZoomer);
                            layerModels.add(new LayerModel(countLayer++, ConstantValue.TEXT_ITEM_TYPE, result, viewZoomer));
                            currentLayer = countLayer;
                            btnLeftMenu.setEnabled(true);
                        }
                    }).show(getSupportFragmentManager().beginTransaction(), "InputDialog");
                    break;
            }
        }
    };

    private void showLeftMenu()
    {
        if (tShirtFragment.getLlLeftMenu().getVisibility() == View.GONE)
        {
            tShirtFragment.getRlRootLayout().bringChildToFront(tShirtFragment.getLlLeftMenu());
            tShirtFragment.getLlLeftMenu().setVisibility(View.VISIBLE);

            tShirtFragment.setTextChangeListener(new TextChangeListener()
            {
                @Override
                public void changeColor(String color)
                {
//                    ViewZoomer oldView = layerModels.get(currentLayer).getViewZoomer();
//
//                    String text = oldView.getText();
//                    ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, Color.RED, oldView.getFontDefault());
//                    viewZoomer.setmPosX(oldView.getmPosX());
//                    viewZoomer.setmPosY(oldView.getmPosY());
//                    viewZoomer.setmScaleFactor(oldView.getmScaleFactor());
//
//                    currentZoomView.set(currentLayer, viewZoomer);
//                    tShirtFragment.getRlRootLayout().removeView(oldView);
//                    tShirtFragment.getRlRootLayout().addView(viewZoomer);
//
//                    layerModels.set(currentLayer, new LayerModel(countLayer++, ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                    PopupMenu popup = new PopupMenu(MyActivity.this, btnCheckout);
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            Toast.makeText(MyActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    popup.show();
                }

                @Override
                public void changeText(String text)
                {
                    ViewZoomer oldView = layerModels.get(currentLayer).getViewZoomer();

                    ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, oldView.getColorDefault(), oldView.getFontDefault());
                    viewZoomer.setmPosX(oldView.getmPosX());
                    viewZoomer.setmPosY(oldView.getmPosY());
                    viewZoomer.setmScaleFactor(oldView.getmScaleFactor());

                    currentZoomView.set(currentLayer, viewZoomer);
                    tShirtFragment.getRlRootLayout().removeView(oldView);
                    tShirtFragment.getRlRootLayout().addView(viewZoomer);

                    layerModels.set(currentLayer, new LayerModel(countLayer++, ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                }

                @Override
                public void changeFont(String font)
                {
                    ViewZoomer oldView = layerModels.get(currentLayer).getViewZoomer();

                    String text = oldView.getText();
                    ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, oldView.getColorDefault(), "chunk.ttf");
                    viewZoomer.setmPosX(oldView.getmPosX());
                    viewZoomer.setmPosY(oldView.getmPosY());
                    viewZoomer.setmScaleFactor(oldView.getmScaleFactor());

                    currentZoomView.set(currentLayer, viewZoomer);
                    tShirtFragment.getRlRootLayout().removeView(oldView);
                    tShirtFragment.getRlRootLayout().addView(viewZoomer);

                    layerModels.set(currentLayer, new LayerModel(countLayer++, ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                }
            });
        }
        else
        {
            tShirtFragment.getLlLeftMenu().setVisibility(View.GONE);
            if (currentLayer != -1)
            {
                layerModels.get(currentLayer).getViewZoomer().setEnabled(true);
            }
        }
    }

    private void showRightMenu()
    {
        if (tShirtFragment.getLlRightMenu().getVisibility() == View.GONE)
        {
            LayerModel[] layers = new LayerModel[layerModels.size()];
            layers = layerModels.toArray(layers);
            LayerArrayAdapter adapter = new LayerArrayAdapter(MyActivity.this, R.id.menu_right_lvListLayer, layers);
            lvListLayer = tShirtFragment.getLvListLayer();
            lvListLayer.setAdapter(adapter);
            lvListLayer.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    Toast.makeText(getApplicationContext(), "item " + i, Toast.LENGTH_SHORT).show();
                    layerModels.get(i).getViewZoomer().setEnabled(true);
                    currentLayer = i;
                    tShirtFragment.getLlRightMenu().setVisibility(View.GONE);
                    if (layerModels.get(i).getType() == ConstantValue.TEXT_ITEM_TYPE)
                    {
                        btnLeftMenu.setEnabled(true);
                    }
                    if (layerModels.get(i).getType() == ConstantValue.IMAGE_ITEM_TYPE)
                    {
                        btnLeftMenu.setEnabled(false);
                    }
                }
            });
            for (LayerModel layer : layerModels)
            {
                layer.getViewZoomer().setEnabled(false);
            }
            tShirtFragment.getRlRootLayout().bringChildToFront(tShirtFragment.getLlRightMenu());
            tShirtFragment.getLlRightMenu().setVisibility(View.VISIBLE);
        }
        else
        {
            tShirtFragment.getLlRightMenu().setVisibility(View.GONE);
            if (currentLayer != -1)
            {
                layerModels.get(currentLayer).getViewZoomer().setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != Activity.RESULT_OK)
        {
            return;
        }
        switch (requestCode)
        {
            case ConstantValue.PICK_FROM_FILE:
                Uri mImageCaptureUri = data.getData();
                mImageCaptureUri = Uri.fromFile(new File(UtilImage.getRealPathFromURI(this, mImageCaptureUri)));
                Bitmap icon = BitmapFactory.decodeFile(mImageCaptureUri.getPath());
                addZoomAndModelLayout(icon);
                break;
            case ConstantValue.CAPTURE_PICTURE:
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                addZoomAndModelLayout(photo);
        }
    }

    private void addZoomAndModelLayout(Bitmap icon)
    {
        ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), scaleImage(icon, 200, 200));
        tShirtFragment.getRlRootLayout().addView(viewZoomer);
        currentZoomView.add(viewZoomer);
        String name = getResources().getResourceEntryName(R.drawable.bt_red_popup_small);
        layerModels.add(new LayerModel(countLayer++, ConstantValue.IMAGE_ITEM_TYPE, name, viewZoomer));
        currentLayer = countLayer;
        btnLeftMenu.setEnabled(false);
    }

    private boolean onTouchShirt(View view, MotionEvent motionEvent)
    {
        layoutParams = (LayoutParams) view.getLayoutParams();
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                home_x = (int) motionEvent.getRawX();
                home_y = (int) motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x_moved = (int) motionEvent.getRawX();
                int y_moved = (int) motionEvent.getRawY();

                layoutParams.leftMargin = (x_moved >= home_x) ? x_moved - home_x : home_x - x_moved;
                layoutParams.topMargin = (y_moved >= home_y) ? y_moved - home_y : home_y - y_moved;

                view.setLayoutParams(layoutParams);
                break;
            case MotionEvent.ACTION_UP:
                layoutParams.leftMargin = 0;
                layoutParams.topMargin = 0;
                view.setLayoutParams(layoutParams);
                break;
        }
        return true;
    }

    public void createOrUpdateFragment(String fragmentTag)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        tShirtFragment = (TShirtFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);

        if (tShirtFragment == null)
        {
            createNewFragment(fragmentTag);
            tShirtFragment.setRetainInstance(true);
            ft.replace(R.id.embed_shirt, tShirtFragment, fragmentTag).addToBackStack(null);
            ft.commit();
        }
        else
        {
            ft.replace(R.id.embed_shirt, tShirtFragment).addToBackStack(null);
            ft.commit();
        }
    }

    private void createNewFragment(String fragmentTag)
    {
        if (fragmentTag.equals(LEFT_TAG))
        {
            tShirtFragment = new LeftTShirtFragment();
        }
        else if (fragmentTag.equals(FRONT_TAG))
        {
            tShirtFragment = new FrontTShirtFragment();
        }
        else if (fragmentTag.equals(BACK_TAG))
        {
            tShirtFragment = new BackTShirtFragment();
        }
        else if (fragmentTag.equals(RIGHT_TAG))
        {
            tShirtFragment = new RightTShirtFragment();
        }
    }


    public void saveState(String sideTag)
    {
        zoomViewsMap.put(sideTag, currentZoomView);
        currentZoomView = new ArrayList<View>();
    }

    public List<View> getView(String sideTag)
    {
        return zoomViewsMap.get(sideTag);
    }

    public void setCurrentZoomView(List<View> views)
    {
        currentZoomView = views;

    }

    private Bitmap scaleImage(Bitmap bitmap, int maxWidth, int maxHeight)
    {
//        int widthBitmap = bitmap.getWidth();
//        int heightBitmap = bitmap.getHeight();
//        if (widthBitmap > heightBitmap)
//        {
//            return Bitmap.createScaledBitmap(bitmap, 200, heightBitmap * 200 / widthBitmap, true);
//        }
//        else
//        {
//            return Bitmap.createScaledBitmap(bitmap, widthBitmap*200/heightBitmap, 200, true);
//        }
        double ratioX = (double) maxWidth / bitmap.getWidth();
        double ratioY = (double) maxHeight / bitmap.getHeight();
        double ratio = Math.min(ratioX, ratioY);

        int newWidth = (int) (bitmap.getWidth() * ratio);
        int newHeight = (int) (bitmap.getHeight() * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

    }

    @Override
    public void onBackPressed()
    {
        if (exist)
        {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        else
        {
            exist = true;
            Toast.makeText(this, "Back press a time to exist", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.free.tshirtdesigner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.free.tshirtdesigner.action.InputActionListener;
import com.free.tshirtdesigner.action.TextChangeListener;
import com.free.tshirtdesigner.adapter.LayerArrayAdapter;
import com.free.tshirtdesigner.dialog.InputDialog;
import com.free.tshirtdesigner.model.LayerModel;
import com.free.tshirtdesigner.util.LocationComponent;
import com.free.tshirtdesigner.util.UtilImage;
import com.free.tshirtdesigner.util.setting.ConstantValue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends FragmentActivity
{
    boolean exist = false;
    private static final int CHECKOUT_CODE = 100;
    private Button btAddImage;
    //    private RelativeLayout rlRootLayout;
    private Button btnCheckout;
    private Button btnQuantity;

    private Button btnLeftMenu;
    private Button btnRightMenu;
    private Button btAddText;
    private ImageView btSave;
    private ImageView btNew;
    private Button btMenu;
    private PopupWindow pwindo;

    public static final String DEFAULT_COLOR = "white";
    public static final int FRONT_TAG = 0;
    public static final int RIGHT_TAG = 1;
    public static final int BACK_TAG = 2;
    public static final int LEFT_TAG = 3;
    public static String colors = DEFAULT_COLOR;


    TShirtFragment tShirtFragment;
    private int[] countLayer = new int[4];
    private ListView lvListLayer;
    private int[] currentLayer = new int[4];
    private List<List<LayerModel>> layerModels = new ArrayList<List<LayerModel>>();
    Map<String, List<View>> zoomViewsMap = new HashMap<String, List<View>>();
    List<View> currentZoomView = new ArrayList<View>();
    public int currentSide;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setupNewTShirt();
        createOrUpdateFragment(FRONT_TAG);
        setUpViewById();
        setActionListener();
    }

    private void setupNewTShirt()
    {
        layerModels.add(new ArrayList<LayerModel>());
        layerModels.add(new ArrayList<LayerModel>());
        layerModels.add(new ArrayList<LayerModel>());
        layerModels.add(new ArrayList<LayerModel>());

        for (int i = 0; i < 4; i++)
        {
            currentLayer[i] = -1;
            countLayer[i] = -1;
        }

        currentSide = FRONT_TAG;
    }

    public void rotateLeft(View view)
    {
        currentSide++;
        if (currentSide > LEFT_TAG)
        {
            currentSide = FRONT_TAG;
        }
        createOrUpdateFragment(currentSide);
    }

    public void rotateRight(View view)
    {
        currentSide--;
        if (currentSide < FRONT_TAG)
        {
            currentSide = LEFT_TAG;
        }
        createOrUpdateFragment(currentSide);
    }

    public void createOrUpdateFragment(int fragmentTag, boolean... isRefresh)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        tShirtFragment = (TShirtFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(fragmentTag));

        if (tShirtFragment != null && isRefresh.length == 0)
        {
            ft.replace(R.id.embed_shirt, tShirtFragment).addToBackStack(null);
            ft.commit();
        }
        else
        {
            createNewFragment(fragmentTag);
            tShirtFragment.setRetainInstance(true);
            ft.replace(R.id.embed_shirt, tShirtFragment, String.valueOf(fragmentTag)).addToBackStack(null);
            ft.commit();
        }

    }

    private void setUpViewById()
    {
        btnCheckout = (Button) findViewById(R.id.header_btCheckout);
        btnQuantity = (Button) findViewById(R.id.header_btChangeQuantity);

        btnRightMenu = (Button) findViewById(R.id.footer_control_btShowRightMenu);
        btnLeftMenu = (Button) findViewById(R.id.footer_control_btShowLeftMenu);
        btnLeftMenu.setEnabled(false);
        btAddImage = (Button) findViewById(R.id.footer_control_btAddImage);
        btAddText = (Button) findViewById(R.id.footer_control_btAddText);
        btNew = (ImageView) findViewById(R.id.btn_new);
        btMenu = (Button) findViewById(R.id.btn_flow);
        btSave = (ImageView) findViewById(R.id.bt_save);
    }

    private void setActionListener()
    {
//        btnCheckout.setOnClickListener(onClickListener);

        btnLeftMenu.setOnClickListener(onClickListener);
        btnRightMenu.setOnClickListener(onClickListener);
        btAddImage.setOnClickListener(onClickListener);
        btAddText.setOnClickListener(onClickListener);
        btNew.setOnClickListener(onClickListener);
        btMenu.setOnClickListener(onClickListener);
        btSave.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.footer_control_btAddImage:
                    PopupMenu popup = new PopupMenu(MyActivity.this, btAddImage);
                    popup.getMenuInflater().inflate(R.menu.image_popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            if (item.getTitle().equals("TAKE PICTURE"))
                            {
                                takeNewImage();
                            }
                            if (item.getTitle().equals("CHOOSE FILE"))
                            {
                                chooseImageFile();
                            }
                            return true;
                        }
                    });
                    popup.show();
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
                            currentZoomView.add(0, viewZoomer);
                            tShirtFragment.getRlRootLayout().addView(viewZoomer);
                            layerModels.get(currentSide).add(new LayerModel(countLayer[currentSide]++, ConstantValue.TEXT_ITEM_TYPE, result, viewZoomer));
                            if (layerModels.get(currentSide).size() > 0)
                            {
                                for (LayerModel layer : layerModels.get(currentSide))
                                {
                                    layer.getViewZoomer().setEnabled(false);
                                    layer.getViewZoomer().setBackgroundNull();
                                }
                            }
                            currentLayer[currentSide] = countLayer[currentSide];
                            layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setEnabled(true);
                            layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setBackground();
                            //todo
                            btnLeftMenu.setEnabled(true);
                            tShirtFragment.showMenuLeft(ConstantValue.TEXT_ITEM_TYPE);
                        }
                    }).show(getSupportFragmentManager().beginTransaction(), "InputDialog");
                    break;
                case R.id.btn_new:
                    resetTshirt();
                    break;
                case R.id.btn_flow:
                    initiatePopupWindow();
                    break;
                case R.id.checkout:
                    Intent intent = new Intent(MyActivity.this, CheckoutActivity.class);
                    startActivityForResult(intent, CHECKOUT_CODE);
                    break;
                case R.id.email:
                    break;
                case R.id.bt_save:
                    doSave();
            }
        }
    };

    private void doSave()
    {
        for (int i = 0; i < 4; i++)
        {
            createOrUpdateFragment(i);
            if (tShirtFragment.rlRootLayout != null)
            {
                tShirtFragment.saveTShirt(getApplicationContext(), i);
            }
        }
        Toast.makeText(this, "SAVE T SHIRT SUCCESSFUL", Toast.LENGTH_LONG).show();


    }

    private void resetTshirt()
    {
        colors = DEFAULT_COLOR;
        zoomViewsMap.clear();
        currentZoomView.clear();
        currentLayer = new int[4];
        countLayer = new int[4];
        layerModels.clear();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        setupNewTShirt();
        createOrUpdateFragment(FRONT_TAG, true);
    }

    private void takeNewImage()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, ConstantValue.CAPTURE_PICTURE);
    }

    private void chooseImageFile()
    {
        Intent intentGallery = new Intent();
        intentGallery.setType("image/*");
        intentGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentGallery, "Complete action using"), ConstantValue.PICK_FROM_FILE);
    }

    private void showLeftMenu()
    {
        if (tShirtFragment.getLlLeftMenu().getVisibility() == View.GONE && currentLayer[currentSide] != -1)
        {
            tShirtFragment.getRlRootLayout().bringChildToFront(tShirtFragment.getLlLeftMenu());
            tShirtFragment.getLlLeftMenu().setVisibility(View.VISIBLE);
            if (currentLayer[currentSide] != -1)
            {
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setEnabled(false);
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setBackgroundNull();
            }
            tShirtFragment.setTextChangeListener(new TextChangeListener()
            {
                @Override
                public void changeColor()
                {
                    PopupMenu popup = new PopupMenu(MyActivity.this, btnQuantity);
                    popup.getMenuInflater().inflate(R.menu.color_popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            ViewZoomer oldView = layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer();

                            String text = oldView.getText();
                            int color = ConstantValue.COLORS.get(item.getTitle());
                            ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, color, oldView.getFontDefault());
                            viewZoomer.setmPosX(oldView.getmPosX());
                            viewZoomer.setmPosY(oldView.getmPosY());
                            viewZoomer.setmScaleFactor(oldView.getmScaleFactor());

                            currentZoomView.set(currentLayer[currentSide], viewZoomer);
                            tShirtFragment.getRlRootLayout().removeView(oldView);
                            tShirtFragment.getRlRootLayout().addView(viewZoomer);

                            layerModels.get(currentSide).set(currentLayer[currentSide], new LayerModel(currentLayer[currentSide], ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                            return true;
                        }
                    });
                    popup.show();
                }

                @Override
                public void changeText(String text)
                {
                    ViewZoomer oldView = layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer();

                    ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, oldView.getColorDefault(), oldView.getFontDefault());
                    viewZoomer.setmPosX(oldView.getmPosX());
                    viewZoomer.setmPosY(oldView.getmPosY());
                    viewZoomer.setmScaleFactor(oldView.getmScaleFactor());

                    currentZoomView.set(currentLayer[currentSide], viewZoomer);
                    tShirtFragment.getRlRootLayout().removeView(oldView);
                    tShirtFragment.getRlRootLayout().addView(viewZoomer);

                    layerModels.get(currentSide).set(currentLayer[currentSide], new LayerModel(currentLayer[currentSide], ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                }

                @Override
                public void changeFont()
                {
                    PopupMenu popup = new PopupMenu(MyActivity.this, btnQuantity);
                    popup.getMenuInflater().inflate(R.menu.font_popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            ViewZoomer oldView = layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer();

                            String text = oldView.getText();
                            String font = ConstantValue.FONTS.get(item.getTitle());
                            ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), text, oldView.getColorDefault(), font);
                            viewZoomer.setmPosX(oldView.getmPosX());
                            viewZoomer.setmPosY(oldView.getmPosY());
                            viewZoomer.setmScaleFactor(oldView.getmScaleFactor());

                            currentZoomView.set(currentLayer[currentSide], viewZoomer);
                            tShirtFragment.getRlRootLayout().removeView(oldView);
                            tShirtFragment.getRlRootLayout().addView(viewZoomer);

                            layerModels.get(currentSide).set(currentLayer[currentSide], new LayerModel(currentLayer[currentSide], ConstantValue.TEXT_ITEM_TYPE, text, viewZoomer));
                            return true;
                        }
                    });
                    popup.show();
                }

                @Override
                public void delete()
                {
                    LayerModel layerModel = layerModels.get(currentSide).get(currentLayer[currentSide]);
                    ViewZoomer oldView = layerModel.getViewZoomer();
                    tShirtFragment.getRlRootLayout().removeView(oldView);
                    layerModels.get(currentSide).remove(layerModel);
                    currentLayer[currentSide] = -1;
                    countLayer[currentSide]--;
                    if (countLayer[currentSide] == -1)
                    {
                        btnLeftMenu.setEnabled(false);
                    }
                    currentZoomView.remove(currentSide);
                }
            });
        }
        else
        {
            tShirtFragment.getLlLeftMenu().setVisibility(View.GONE);
            if (currentLayer[currentSide] != -1)
            {
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setEnabled(true);
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setBackground();
            }
        }
    }

    private void showRightMenu()
    {
        if (tShirtFragment.getLlRightMenu().getVisibility() == View.GONE)
        {
            LayerModel[] layers = new LayerModel[layerModels.get(currentSide).size()];
            layers = layerModels.get(currentSide).toArray(layers);
            LayerArrayAdapter adapter = new LayerArrayAdapter(MyActivity.this, R.id.menu_right_lvListLayer, layers);
            lvListLayer = tShirtFragment.getLvListLayer();
            lvListLayer.setAdapter(adapter);
            lvListLayer.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    Toast.makeText(getApplicationContext(), "item " + i, Toast.LENGTH_SHORT).show();
                    layerModels.get(currentSide).get(i).getViewZoomer().setEnabled(true);
                    layerModels.get(currentSide).get(i).getViewZoomer().setBackground();
                    currentLayer[currentSide] = i;
                    tShirtFragment.getLlRightMenu().setVisibility(View.GONE);
                    tShirtFragment.getRlRootLayout().bringChildToFront(layerModels.get(currentSide).get(i).getViewZoomer());
                    if (layerModels.get(currentSide).get(i).getType() == ConstantValue.TEXT_ITEM_TYPE)
                    {
                        tShirtFragment.showMenuLeft(ConstantValue.TEXT_ITEM_TYPE);
                    }
                    if (layerModels.get(currentSide).get(i).getType() == ConstantValue.IMAGE_ITEM_TYPE)
                    {
                        tShirtFragment.showMenuLeft(ConstantValue.IMAGE_ITEM_TYPE);
                    }
                }
            });
            for (LayerModel layer : layerModels.get(currentSide))
            {
                layer.getViewZoomer().setEnabled(false);
                layer.getViewZoomer().setBackgroundNull();
            }
            tShirtFragment.getRlRootLayout().bringChildToFront(tShirtFragment.getLlRightMenu());
            tShirtFragment.getLlRightMenu().setVisibility(View.VISIBLE);
        }
        else
        {
            tShirtFragment.getLlRightMenu().setVisibility(View.GONE);
            if (currentLayer[currentSide] != -1)
            {
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setEnabled(true);
                layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setBackground();
            }
        }
    }

    public void changeToEditAble(View view)
    {
        for (LayerModel layer : layerModels.get(currentSide))
        {
            layer.getViewZoomer().setEnabled(false);
            layer.getViewZoomer().setBackgroundNull();
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
                break;
        }
    }

    //todo
    private void addZoomAndModelLayout(Bitmap icon)
    {
        ViewZoomer viewZoomer = new ViewZoomer(getApplicationContext(), UtilImage.scaleImage(icon, 200, 200));
        tShirtFragment.getRlRootLayout().addView(viewZoomer);

        currentZoomView.add(0, viewZoomer);
        String name = getResources().getResourceEntryName(R.drawable.bt_red_popup_small);
        layerModels.get(currentSide).add(new LayerModel(countLayer[currentSide]++, ConstantValue.IMAGE_ITEM_TYPE, name, viewZoomer));
        if (layerModels.get(currentSide).size() > 0)
        {
            for (LayerModel layer : layerModels.get(currentSide))
            {
                layer.getViewZoomer().setEnabled(false);
                layer.getViewZoomer().setBackgroundNull();
            }
        }
        currentLayer[currentSide] = countLayer[currentSide];
        layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setEnabled(true);
        layerModels.get(currentSide).get(currentLayer[currentSide]).getViewZoomer().setBackground();
        btnLeftMenu.setEnabled(true);
        tShirtFragment.showMenuLeft(ConstantValue.IMAGE_ITEM_TYPE);
    }

    private void createNewFragment(int fragmentTag)
    {
        if (fragmentTag == LEFT_TAG)
        {
            tShirtFragment = new LeftTShirtFragment();
        }
        else if (fragmentTag == FRONT_TAG)
        {
            tShirtFragment = new FrontTShirtFragment();
        }
        else if (fragmentTag == BACK_TAG)
        {
            tShirtFragment = new BackTShirtFragment();
        }
        else if (fragmentTag == RIGHT_TAG)
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

    private void initiatePopupWindow()
    {
        try
        {
            LayoutInflater inflater = (LayoutInflater) MyActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.header_options_menu, (ViewGroup) findViewById(R.id.equipment_detail_menu_option_elements));
            pwindo = new PopupWindow(layout, 150, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pwindo.setBackgroundDrawable(new BitmapDrawable());
            pwindo.setOutsideTouchable(true);
            pwindo.setFocusable(true);
            Rect location = LocationComponent.getLocationSpec(findViewById(R.id.btn_flow), pwindo);
            Rect location_ivLine = LocationComponent.getLocation(findViewById(R.id.iv_line));
            pwindo.showAtLocation(layout, Gravity.TOP | Gravity.RIGHT, location.left, location_ivLine.top);
            layout.findViewById(R.id.email).setOnClickListener(onClickListener);
            layout.findViewById(R.id.checkout).setOnClickListener(onClickListener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

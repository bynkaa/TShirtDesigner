package com.free.tshirtdesigner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.free.tshirtdesigner.action.ColorChooserInterface;
import com.free.tshirtdesigner.adapter.GridViewAdapter;
import com.free.tshirtdesigner.util.UtilImage;
import com.free.tshirtdesigner.util.UtilResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * User: binhtv
 * Date: 2/25/14
 * Time: 11:07 PM
 */
public class TShirtFragment extends Fragment
{
    RelativeLayout rlRootLayout;
    ImageView ivShirt;
    LinearLayout llRightMenu;
    GridView gvColorChooser;
    String colors = "white";
    public int tShirtDirection;
    String sideTag;
    private ListView lvListLayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rlRootLayout = (RelativeLayout) inflater.inflate(R.layout.shirt_layout, container, false);
        ivShirt = (ImageView) rlRootLayout.findViewById(R.id.ivShirt);
        ivShirt.setImageResource(R.drawable.tshirt_front_500);
        ivShirt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return false;
            }
        });

        //right menu
        llRightMenu = (LinearLayout) rlRootLayout.findViewById(R.id.right_menu_llRoot);
        gvColorChooser = (GridView) rlRootLayout.findViewById(R.id.right_menu_gvColorChooser);

        gvColorChooser.setAdapter(new GridViewAdapter(getActivity(), new ColorChooserInterface()
        {
            @Override
            public void itemClick(String colorNameSelected)
            {
                int colorSelectedId = UtilResource.getStringIdByName(getActivity(), colorNameSelected);
                colors = getResources().getString(colorSelectedId);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), tShirtDirection);
                bitmap = UtilImage.grayScaleImage(bitmap, colors);
                ivShirt.setImageBitmap(bitmap);
            }
        }));

        lvListLayer = (ListView) rlRootLayout.findViewById(R.id.menu_right_lvListLayer);

        showDirectionTShirt();
        // get old view
        List<View> viewList = getMainActivity().getView(sideTag);
        if (viewList != null && viewList.size() > 0)
        {
            for (View zoomView : viewList)
            {
                rlRootLayout.addView(zoomView);
            }
            getMainActivity().setCurrentZoomView(viewList);


        }
        return rlRootLayout;
    }

    public RelativeLayout getRlRootLayout()
    {
        return rlRootLayout;
    }

    public GridView getGvColorChooser()
    {
        return gvColorChooser;
    }

    public ImageView getIvShirt()
    {
        return ivShirt;
    }

    public LinearLayout getLlRightMenu()
    {
        return llRightMenu;
    }

    private void showDirectionTShirt()
    {
        if (!colors.equals("white"))
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), tShirtDirection);
            bitmap = UtilImage.grayScaleImage(bitmap, colors);
            ivShirt.setImageBitmap(bitmap);
        }
        else
        {
            ivShirt.setImageResource(tShirtDirection);
        }
    }

    public void saveTShirt(Context context)
    {
        Bitmap bitmap = rlRootLayout.getDrawingCache();
        File file, f = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            file = new File(Environment.getExternalStorageDirectory(), "Tshirt_cache");
            if (!file.exists())
            {
                file.mkdirs();
            }
            f = new File(file.getAbsolutePath() + File.separator + "front" + ".png");
        }
        try
        {
            FileOutputStream outputStream = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, outputStream);
            outputStream.close();
            Toast.makeText(context, "SAVE T SHIRT DONE", Toast.LENGTH_LONG).show();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (rlRootLayout != null)
        {
            rlRootLayout.removeAllViewsInLayout();
        }
        getMainActivity().saveState(sideTag);
    }

    public MyActivity getMainActivity()
    {
        return (MyActivity) getActivity();
    }

    public ListView getLvListLayer()
    {
        return lvListLayer;
    }
}

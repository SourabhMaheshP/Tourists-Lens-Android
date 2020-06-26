package com.example.touristslens;

/**Machine Learning Model reference
 *Model_Info - Deep-learning Model
 *Model is a CNN model -- classified on three monuments namely, (India Gate, Qutub Minar, Tajmahal)*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MLModel {

    private Context mContext;
    private TensorImage mInputImageBuffer;
    private TensorBuffer mOutputProbabilityBuffer;
    protected Interpreter mTflite;
    private MappedByteBuffer mTfliteModel;
    private Bitmap mInputImage;
    private BitmapDrawable mBitmapDrawable;
    private List<String> labels;

    private int mImageSizeX,mImageSizeY;
    private float mResult[][] = new float[1][ApplicationConstants.NO_OF_CLASSES];

    public MLModel(Context context)
    {
        mContext = context;
        try {
            //loading tflite model- classifying monuments
            mTfliteModel = FileUtil.loadMappedFile(mContext, ApplicationConstants.MODEL_PATH);
            Log.e("TfliteModel","Success:"+mTfliteModel);

            //loading labels [names of monument]
            //include- optional
            labels = FileUtil.loadLabels(mContext, ApplicationConstants.LABEL_PATH);
            Log.e("Labels:",""+labels);

        } catch (IOException e) {
            Log.e("TfliteModel","Failed:"+mTfliteModel+e.getMessage());
        }
    }
    public void createInference(Bitmap bitmap)
    {
        mInputImage = bitmap;
        mTflite = new Interpreter(mTfliteModel);
//        Log.e("inference","Done");

        int imageTensorIndex = 0;

        //about model- input image shape - (1,224,224,1)

        int[] imageShape = mTflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        mImageSizeY = imageShape[1];
        mImageSizeX = imageShape[2];
        DataType imageDataType = mTflite.getInputTensor(imageTensorIndex).dataType();

//        Log.e("imagesize:",""+imageShape[0]+","+imageSizeX+","+imageSizeY+","+imageShape[3]);
//        imageDataType= FLOAT32
//        Log.e("imagedatatype",imageDataType+"");

        //Probability shape = (1,2) 2= Number of classes [tajmahal,indiagate]
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                mTflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = mTflite.getOutputTensor(probabilityTensorIndex).dataType();

//        Log.e("probshape",""+probabilityShape[0]+","+probabilityShape[1]);
//        probabilityDataType = FLOAT32
//        Log.e("probdatatype",""+probabilityDataType);

        // Creates the input tensor.
        mInputImageBuffer = new TensorImage(imageDataType);

        // Creates the output tensor and its processor.
        mOutputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        //image to grayscale
//        bitmap = toGrayscale(bitmap);

        mInputImageBuffer = loadImage(mInputImage);

        //convert inputimageBuffer to Bitmap
//      bitmap = inputImageBuffer.getBitmap();

        //size of an image after tranformation
//        Log.e("imagesize1:",""+inputImageBuffer.getBitmap().getHeight()+","+inputImageBuffer.getBitmap().getWidth());

        //Simple option is to create a float array containg the output values

        try{

            //prediction of an image(JPG)
            mTflite.run(mInputImageBuffer.getTensorBuffer().getBuffer(),mResult);

        }catch(Exception e){
            Log.e("TFLITE output","Failed"+e.getMessage());
        }

        //Log output.
        Log.e("result",""+mResult[0][0]+","+mResult[0][1] + ","+mResult[0][2]);

        /**close tflite interpreter*/
        mTflite.close();
    }
    private TensorImage loadImage(Bitmap bitmap)
    {
        mInputImageBuffer.load(bitmap);

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(ApplicationConstants.reSize, ApplicationConstants.reSize, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .build();
        return imageProcessor.process(mInputImageBuffer);
    }
    private int findMax(float res[][])
    {
        int Max = (int)res[0][0];
        int index = 0;
        for(int i = 1; i< 3; i++)
        {
            if(res[0][i] > Max)
            {
                Max =(int) res[0][i];
                index = i;
            }
        }
        Log.e("index:",""+index);
        return index;
    }

    public String getModelOutput()
    {
        Map<Integer,String> CATEGORIES = new HashMap<>();
        CATEGORIES.put(0,"IndiaGate");
        CATEGORIES.put(1,"QutubMinar");
        CATEGORIES.put(2,"TajMahal");

        int index = findMax(mResult);
        return ""+CATEGORIES.get(index);
    }
}

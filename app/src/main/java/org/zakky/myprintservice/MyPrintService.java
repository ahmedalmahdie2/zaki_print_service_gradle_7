package org.zakky.myprintservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentInfo;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MyPrintService extends PrintService {
    private static final String PRINTER = "iZAM Print Service";
    private static final String PRINTER_ID = "aaa";

    List<PrinterInfo> printers = null;
    PrinterInfo printerInfo = null;
    PrinterInfo.Builder builder = null;
    private final SparseArray<PrintJob> mProcessedPrintJobs = new SparseArray<PrintJob>();
    static final String INTENT_EXTRA_ACTION_TYPE = "INTENT_EXTRA_ACTION_TYPE";
    static final String INTENT_EXTRA_PRINT_JOB_ID = "INTENT_EXTRA_PRINT_JOB_ID";
    static final int ACTION_TYPE_ON_PRINT_JOB_PENDING = 1;
    static final int ACTION_TYPE_ON_REQUEST_CANCEL_PRINT_JOB = 2;

    PrinterInfo mThermalPrinter;


    @Override
    public void onCreate() {
        mThermalPrinter = new PrinterInfo.Builder(generatePrinterId(PRINTER_ID),
                PRINTER, PrinterInfo.STATUS_IDLE).build();
    }

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d("myprinter", "MyPrintService#onCreatePrinterDiscoverySession() called");
        return new ThermalPrinterDiscoverySession(mThermalPrinter);

//        return new PrinterDiscoverySession() {
//            @Override
//            public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
//                Log.d("myprinter", "PrinterDiscoverySession#onStartPrinterDiscovery(priorityList: " + priorityList + ") called");
//
//                if (!priorityList.isEmpty()) {
//                    return;
//                }
//
//                printers = new ArrayList<>();
//                PrinterId printerId = generatePrinterId(PRINTER_ID);
//                builder = new PrinterInfo.Builder(printerId, PRINTER, PrinterInfo.STATUS_IDLE);
//                printerInfo = builder.build();
//
//
//
////                printers.add(printerInfo);
////                addPrinters(printers);
//
//            }
//
//            @Override
//            public void onStopPrinterDiscovery() {
//                Log.d("myprinter", "MyPrintService#onStopPrinterDiscovery() called");
//            }
//
//            @Override
//            public void onValidatePrinters(List<PrinterId> printerIds) {
//                Log.d("myprinter", "MyPrintService#onValidatePrinters(printerIds: " + printerIds + ") called");
//            }
//
//            @Override
//            public void onStartPrinterStateTracking(PrinterId printerId) {
//                Log.d("myprinter", "MyPrintService#onStartPrinterStateTracking(printerId: " + printerId + ") called");
//            }
//
//            @Override
//            public void onStopPrinterStateTracking(PrinterId printerId) {
//                Log.d("myprinter", "MyPrintService#onStopPrinterStateTracking(printerId: " + printerId + ") called");
//
//                builder = new PrinterInfo.Builder(printerId, PRINTER, PrinterInfo.STATUS_IDLE);
//                PrinterCapabilitiesInfo.Builder capBuilder =
//                        new PrinterCapabilitiesInfo.Builder(printerId);
//
//                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
//                capBuilder.addResolution(new PrintAttributes.Resolution(
//                        "Default", "Default", 360, 360), true);
//                capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR
//                                + PrintAttributes.COLOR_MODE_MONOCHROME,
//                        PrintAttributes.COLOR_MODE_COLOR);
//                capBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
//
//                PrinterCapabilitiesInfo caps = capBuilder.build();
//                builder.setCapabilities(caps);
//                printerInfo = builder.build();
//                List<PrinterInfo> printers = new ArrayList();
//                printers.add(printerInfo);
//                addPrinters(printers);
//            }
//
//            @Override
//            public void onDestroy() {
//                Log.d("myprinter", "MyPrintService#onDestroy() called");
//            }
//        };
    }

    void doPrintJobOnBluetoothPrinter(PrintJob printJob)
    {
///////////////////////// create fileInputStream from printJob ///////////////////////////////

        ParcelFileDescriptor[] fileDescriptors = null;

        PrintDocument printDocument = printJob.getDocument();
        PrintDocumentInfo printDocumentInfo = printDocument.getInfo();
        ParcelFileDescriptor parcelFileDescriptor = printJob.getDocument().getData();
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        byte[] bytes = null;
        Bitmap bitmap = null;

        FileInputStream fileInputStream = null;
        try {
//            fileDescriptors = ParcelFileDescriptor.createPipe();


            fileInputStream = new FileInputStream(fileDescriptor);
            Log.d("FileDescriptor", "document info: " + fileDescriptor.toString());
        }
        catch (Exception e)
        {
            Log.d("fileInputStream error", e.getMessage());
        }
//////////////////////////////////////////////////////////////////////////////////////////////

//        val downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)



////////////// convert the FD to bytes according to the size of the print document data size  //////////////
        byte[] fileContentBytes = null;
        try {

            long byteLength = printDocumentInfo.getDataSize();

            /////////////////////////////
//            InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();



                int read = -1;
                byte[] data = new byte[1];

                while ((fileInputStream.read(data, 0, data.length))  != -1) {
                    read = fileInputStream.read(data, 0, data.length);
                    byteArrayOutputStream.write(data, 0, read);
                }

                bytes = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                Log.d("byteResult size", "" + bytes.length);


                //////////////////////////////////////////




            /////////////////////////////////////////

//////////////// convert the InputStream to bitmap //////////////
                try {
                    bitmap = BitmapFactory.decodeStream(fileInputStream);
                    Log.d("bitmap", bitmap != null ? " NOT null" : "NULL!");
                } catch (Exception e)
                {
                    Log.d("error creating bitmap", e.getMessage());
                }
/////////////////////////////////////////////////////////////////////

//                fileInputStream.read(printJob.getDocument().getInfo().getName(), 0,(int) byteLength);
            } catch (Exception e) {
                Log.d("fileInputStream Error", e.toString());
            }
////////////////////////////////////////////////////////////////////////////////////////////////

//////////////// part to write the file and then convert it to bytes  //////////////
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("testFile"));
//            writer.write("This is A Test file print. \n please be cautious as to not consider this a real file!");
//            writer.close();
//
//            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File[] files = dir.listFiles();
//            for (int i = 0; i < files.length; i++) {
//                Log.d("downloads file", files[i].getName() + " Path: " + files[i].getPath());
//                if (files[i].getName() == "sample(1)1584541496.pdf") {
//                    Log.d("downloads file", "Found file to be printer");
//                    fileToBePrinted = new File(files[i].getPath());
//                }
//            }
//
//            if (fileToBePrinted != null) {
//                FileInputStream fileInputStream = new FileInputStream(fileToBePrinted);
//
//                long byteLength = fileToBePrinted.length(); // byte count of the file-content
//
//                fileContentBytes = new byte[(int) byteLength];
//                fileInputStream.read(fileContentBytes, 0, (int) byteLength);
//            }
//        } catch (Exception e)
//        {
//            Log.d("File Exception", e.getMessage());
//        }
///////////////////////////////////////////////////////////////////////////////////////////////////////

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {

            Log.d("doPrintJobOnBluetooth","bluetooth " + printJob.getDocument().getInfo().getName());
            // Your printing code/method HERE
            try{

                ///////////////// some code to account for big files and wait for maybe 2 secs.
///////////////////////////////////////////////////////////////////////////////
//                int targetWidth = 384;//(int) Math.floor(203 * 58 / 25.4); // 48mm printing zone with 203dpi => 383px
////                Bitmap originalBitmap1 = ScreenShot.get(this.getApplicationContext(), webView);
//                try {
//                    Thread.sleep(2000);
//                } catch (Exception e)
//                {
//                    Log.d("getAsyncEscPosPrinter", "couldn't sleep");
//                }
///////////////////////////////////////////////////////////////////////////////

                //// scale down the bitmap ////
///////////////////////////////////////////////////////////////////////////////
//                Bitmap rescaledBitmap = Bitmap.createScaledBitmap(
//                        bitmap,
//                        targetWidth,
//                        Math.round(((float) bitmap.getHeight()) * ((float) targetWidth) / ((float) bitmap.getWidth())),
//                        true
//                );
///////////////////////////////////////////////////////////////////////////////

                EscPosPrinterCommands printerCommands = new EscPosPrinterCommands(BluetoothPrintersConnections.selectFirstPaired());
                try {
                    printerCommands.connect();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.connect()");

                    printerCommands.reset();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.reset()");

//                    Log.d("bitmapToBytes", "before time: " + (new Date()).toString());
//                    Log.d("bitmapToBytes", "after time: " + (new Date()).toString());

//////////////// rest of code to convert inputstream to bitmap then to bytes ///////////////////////
//                    bytes = printerCommands.bitmapToBytes(bitmap);
////////////////////////////////////////////////////////////////////////////////////////////////////
                    Log.d("byteResult size", "" + bytes.length);

                    if(bytes != null && bytes.length > 0) {
//                      byte[] bytes = EscPosPrinterCommands.bitmapToBytes(bitmap);
                        String base64String= Base64.encodeToString(bytes, 0);
                        byte[] bytearray = Base64.decode(base64String, 0);
                        Log.d("bytearray base64", base64String != null ? base64String : "is NULL!");

//                        final Bitmap bitmapFromBytes = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        Log.d("bitmap", bitmapFromBytes != null ? "is NOT null" : "is NULL!");

//                        printerCommands.printImage(bytearray);
//                      printerCommands.printImage("This is A Test file print. \n please be cautious as to not consider this a real file!".getBytes());
//                      printerCommands.printText(base64String);
                        Log.d("getAsyncEscPosPrinter", "printerCommands.printImage()");

                        printerCommands.feedPaper(50);
                        Log.d("getAsyncEscPosPrinter", "printerCommands.feedPaper()");

                        printerCommands.cutPaper();
                        Log.d("getAsyncEscPosPrinter", "printerCommands.cutPaper()");
                    }
                    else
                    {
                        Log.d("getAsyncEscPosPrinter", "empty image bytes");
                    }
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }


//                new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), webView));
            }catch(Exception e)
            {
                Log.d("createWebPrintJob Error",e.toString());
            }
        }
        printJob.complete();
        try{
//            in.close();
        } catch(Exception ee)
        {

        }
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {

        Intent intent = new Intent(MyPrintService.this, MainActivity.class);
        intent.putExtra("data", printJob.getDocument().getData());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        printJob.start();
        startActivity(intent);


//        mProcessedPrintJobs.put(printJob.getId().describeContents(), printJob);
//        if (printJob.isQueued()) {
//            Log.d("myprinter", "queued: " + printJob.getId().toString());
//            Log.d("myprinter", "document info: " + printJob.getDocument().getInfo());
//            printJob.start();
//        }
//
//        ///////////////////////
//
//        try {
////            final byte[] buffer = new byte[4];
////            @SuppressWarnings("unused")
////            final int read = in.read(buffer);
////            Log.d("myprinter", "first " + buffer.length + "bytes of content: " + toString(buffer));
//            doPrintJobOnBluetoothPrinter(printJob);
//        } catch (Exception e) {
//            Log.d("myprinter", e.toString());
//        } finally {
////            try {
////                in.close();
////            } catch (IOException e) {
////                assert true;
////            }
//        }
//        ////////////////////////////
//        printJob.complete();
    }

    private static String toString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(b).append(',');
        }
        if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.d("myprinter", "canceled: " + printJob.getId().toString());

//        mProcessedPrintJobs.put(printJob.getId().describeContents(), printJob);

        //////////////////////
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(INTENT_EXTRA_PRINT_JOB_ID, printJob.getId());
//        intent.putExtra(INTENT_EXTRA_ACTION_TYPE, ACTION_TYPE_ON_REQUEST_CANCEL_PRINT_JOB);
//        startActivity(intent);

        printJob.cancel();
    }

}
class ThermalPrinterDiscoverySession extends PrinterDiscoverySession {

    private PrinterInfo printerInfo;

    ThermalPrinterDiscoverySession(PrinterInfo printerInfo) {
        PrinterCapabilitiesInfo capabilities =
                new PrinterCapabilitiesInfo.Builder(printerInfo.getId())
                        .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                        .addResolution(new PrintAttributes.Resolution("1234","Default",200,200), true)
                        .setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_MONOCHROME)
                        .build();
        this.printerInfo = new PrinterInfo.Builder(printerInfo)
                .setCapabilities(capabilities)
                .build();
    }

    @Override
    public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
        List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
        printers.add(printerInfo);
        addPrinters(printers);
    }

    @Override
    public void onStopPrinterDiscovery() {

    }

    @Override
    public void onValidatePrinters(List<PrinterId> printerIds) {

    }

    @Override
    public void onStartPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onStopPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onDestroy() {

    }
}
package org.zakky.myprintservice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyPrintService extends PrintService {
    private static final String PRINTER = "dummy printer";
    private static final String PRINTER_ID = "aaa";

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d("myprinter", "MyPrintService#onCreatePrinterDiscoverySession() called");

        return new PrinterDiscoverySession() {
            @Override
            public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
                Log.d("myprinter", "PrinterDiscoverySession#onStartPrinterDiscovery(priorityList: " + priorityList + ") called");

                if (!priorityList.isEmpty()) {
                    return;
                }

                List<PrinterInfo> printers = new ArrayList<>();
                PrinterId printerId = generatePrinterId(PRINTER_ID);
                PrinterInfo.Builder builder = new PrinterInfo.Builder(printerId, PRINTER, PrinterInfo.STATUS_IDLE);
                PrinterInfo info = builder.build();

//                PrinterCapabilitiesInfo.Builder capBuilder = new PrinterCapabilitiesInfo.Builder(printerId);
//                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
//                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false);
//                capBuilder.addMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT, false);
//                capBuilder.addResolution(new PrintAttributes.Resolution("resolutionId", "default resolution", 600, 600), false);
//                capBuilder.addResolution(new PrintAttributes.Resolution("resolutionId", "Thermal 58", 58, 600), true);
//                capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR | PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_COLOR);
//                builder.setCapabilities(capBuilder.build());
                printers.add(info);
                addPrinters(printers);

            }

            @Override
            public void onStopPrinterDiscovery() {
                Log.d("myprinter", "MyPrintService#onStopPrinterDiscovery() called");
            }

            @Override
            public void onValidatePrinters(List<PrinterId> printerIds) {
                Log.d("myprinter", "MyPrintService#onValidatePrinters(printerIds: " + printerIds + ") called");
            }

            @Override
            public void onStartPrinterStateTracking(PrinterId printerId) {
                Log.d("myprinter", "MyPrintService#onStartPrinterStateTracking(printerId: " + printerId + ") called");
            }

            @Override
            public void onStopPrinterStateTracking(PrinterId printerId) {
                Log.d("myprinter", "MyPrintService#onStopPrinterStateTracking(printerId: " + printerId + ") called");
                PrinterInfo.Builder builder = new PrinterInfo.Builder(printerId,
                        PRINTER, PrinterInfo.STATUS_IDLE);
                PrinterCapabilitiesInfo.Builder capBuilder =
                        new PrinterCapabilitiesInfo.Builder(printerId);

                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
                capBuilder.addResolution(new PrintAttributes.Resolution(
                        "Default", "Default", 360, 360), true);
                capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR
                                + PrintAttributes.COLOR_MODE_MONOCHROME,
                        PrintAttributes.COLOR_MODE_COLOR);
                capBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

                PrinterCapabilitiesInfo caps = capBuilder.build();
                builder.setCapabilities(caps);
                PrinterInfo info = builder.build();
                List<PrinterInfo> infos = new ArrayList<PrinterInfo>();
                infos.add(info);
                addPrinters(infos);
            }

            @Override
            public void onDestroy() {
                Log.d("myprinter", "MyPrintService#onDestroy() called");
            }
        };
    }

    void doPrintJobOnBluetoothPrinter(PrintJob printJob)
    {
//        final FileInputStream in = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());

//        BufferedInputStream bis = new BufferedInputStream(in);

//        FileDescriptor fileDesc = printJob.getDocument().getData().getFileDescriptor();
//        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDesc);

//        val downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//        File fileToBePrinted = null;
//        byte[] fileContentBytes = null;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {

            Log.d("doPrintJobOnBluetooth","bluetooth " + printJob.getDocument().getInfo().getName());
            // Your printing code/method HERE
            try{

                int targetWidth = 384;//(int) Math.floor(203 * 58 / 25.4); // 48mm printing zone with 203dpi => 383px
//                Bitmap originalBitmap1 = ScreenShot.get(this.getApplicationContext(), webView);
                try {
                    Thread.sleep(2000);
                } catch (Exception e)
                {
                    Log.d("getAsyncEscPosPrinter", "couldn't sleep");
                }
//                Bitmap rescaledBitmap = Bitmap.createScaledBitmap(
//                        bitmap,
//                        targetWidth,
//                        Math.round(((float) bitmap.getHeight()) * ((float) targetWidth) / ((float) bitmap.getWidth())),
//                        true
//                );


                EscPosPrinterCommands printerCommands = new EscPosPrinterCommands(BluetoothPrintersConnections.selectFirstPaired());
                try {
                    printerCommands.connect();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.connect()");

                    printerCommands.reset();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.reset()");

//                    Log.d("bitmapToBytes", "before time: " + (new Date()).toString());
//                    byte[] bytes = EscPosPrinterCommands.bitmapToBytes(bitmap);
//                    Log.d("bitmapToBytes", "after time: " + (new Date()).toString());

//                    if(fileContentBytes != null && fileContentBytes.length > 0) {
//                        printerCommands.printImage(fileContentBytes);
                        printerCommands.printImage("This is A Test file print. \n please be cautious as to not consider this a real file!".getBytes());
//                        printerCommands.printText("Hello Printer");
                        Log.d("getAsyncEscPosPrinter", "printerCommands.printImage()");

                        printerCommands.feedPaper(50);
                        Log.d("getAsyncEscPosPrinter", "printerCommands.feedPaper()");

                        printerCommands.cutPaper();
                        Log.d("getAsyncEscPosPrinter", "printerCommands.cutPaper()");
//                    }
//                    else
//                    {
//                        Log.d("getAsyncEscPosPrinter", "empty image bytes");
//
//                    }
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }


//                new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), webView));
            }catch(Exception e)
            {
                try{
//                    in.close();
                } catch(Exception ee)
                {

                }
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
        Log.d("myprinter", "queued: " + printJob.getId().toString());

        Log.d("myprinter", "document info: " + printJob.getDocument().getInfo());

//        final PrintDocument document = printJob.getDocument();
//        final FileInputStream in = new FileInputStream(document.getData().getFileDescriptor());

//      printJob.start();

        try {
//            final byte[] buffer = new byte[4];
//            @SuppressWarnings("unused")
//            final int read = in.read(buffer);
//            Log.d("myprinter", "first " + buffer.length + "bytes of content: " + toString(buffer));
            doPrintJobOnBluetoothPrinter(printJob);
        } catch (Exception e) {
            Log.d("myprinter", "", e);
        } finally {
//            try {
//                in.close();
//            } catch (IOException e) {
//                assert true;
//            }
        }
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

        printJob.cancel();
    }

}

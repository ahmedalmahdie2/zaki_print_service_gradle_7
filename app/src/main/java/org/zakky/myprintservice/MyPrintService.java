package org.zakky.myprintservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentInfo;
import android.print.PrintJobInfo;
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

import net.sf.andpdf.nio.ByteBuffer;

import org.zakky.myprintservice.util.PrintUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
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
    }

    void doPrintJobOnBluetoothPrinter(PrintJob printJob)
    {
        byte[] bytes = null;
        Bitmap bitmap = null;


        if (printJob.isQueued()) {
            printJob.start();
        }
        final PrintJobInfo info = printJob.getInfo();
        final File file = new File(getFilesDir(), info.getLabel());


        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();

        } catch (IOException ioe) {

        }


        FileChannel channel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            ByteBuffer bb = ByteBuffer.NEW(channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size()));
            byte[] tempBytes = new byte[bb.remaining()];
            bb.get(tempBytes, 0, tempBytes.length);

            String base64String= Base64.encodeToString(tempBytes, 0);
            byte[] tempBytes02 = Base64.decode(base64String, 0);
            Log.d("bytearray base64", base64String != null ? base64String : "is NULL!");
            FileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            val page = pdfRenderer.openPage(pageNumber)

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            pdfRenderer.close()

            return bitmap

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {

            Log.d("doPrintJobOnBluetooth","bluetooth " + printJob.getDocument().getInfo().getName());
            // Your printing code/method HERE
            try{
                EscPosPrinterCommands printerCommands = new EscPosPrinterCommands(BluetoothPrintersConnections.selectFirstPaired());
                try {
                    printerCommands.connect();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.connect()");

                    printerCommands.reset();
                    Log.d("getAsyncEscPosPrinter", "printerCommands.reset()");

//////////////// rest of code to convert inputstream to bitmap then to bytes ///////////////////////
//                    Bitmap rescaledBitmap = Bitmap.createScaledBitmap(
//                        bitmap,
//                        384,
//                        Math.round(((float) bitmap.getHeight()) * ((float) 384) / ((float) bitmap.getWidth())),
//                        true
//                    );

                    if(bitmap != null) {
                        bytes = printerCommands.bitmapToBytes(bitmap);
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////
                    Log.d("byteResult size", "" + bytes.length);

                    if(bytes != null && bytes.length > 0) {

                        printerCommands.printImage(bytes);
//                      printerCommands.printImage("This is A Test file print. \n please be cautious as to not consider this a real file!".getBytes());
//                      printerCommands.printText(base64String);
                        Log.d("getAsyncEscPosPrinter", "printerCommands.printImage()");

                        printerCommands.feedPaper(255);
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
//        mProcessedPrintJobs.put(printJob.getId().describeContents(), printJob);
        if (printJob.isQueued()) {
            Log.d("myprinter", "queued: " + printJob.getId().toString());
            Log.d("myprinter", "document info: " + printJob.getDocument().getInfo());
            printJob.start();
        }

        doPrintJobOnBluetoothPrinter(printJob);
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.d("myprinter", "canceled: " + printJob.getId().toString());
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
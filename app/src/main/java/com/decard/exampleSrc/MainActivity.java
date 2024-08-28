package com.decard.exampleSrc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.decard.NDKMethod.BasicOper;
import com.decard.driver.utils.HexDump;
import com.decard.entitys.IDCard;
import com.decard.exampleSrc.desfire.DESFireEV1;
import com.decard.exampleSrc.desfire.DESFireEV3;
import com.decard.exampleSrc.desfire.ev1.model.VersionInfo;
import com.decard.exampleSrc.desfire.ev1.model.command.DefaultIsoDepAdapter;
import com.decard.exampleSrc.desfire.ev1.model.command.Utils;
import com.decard.exampleSrc.desfire.ev1.model.key.DesfireKeyType;
import com.decard.exampleSrc.felica.FeliCa;
import com.decard.exampleSrc.felica.Util;
import com.decard.exampleSrc.mifarePlus.MifarePlus;
import com.decard.exampleSrc.reader.P18QDesfireEV;
import com.decard.exampleSrc.reader.P18QMifarePlus;
import com.decard.exampleSrc.samav2.SAMP18Q;
import com.decard.exampleSrc.samav2.ByteArrayTools;
import com.decard.exampleSrc.ui.CreateApplicationActivity;
import com.decard.exampleSrc.ui.CreateFileActivity;
import com.decard.exampleSrc.ui.DesfireMainActivity;
import com.decard.exampleSrc.ui.FormatPiccActivity;
import com.decard.exampleSrc.ui.PersonalizeActivity;
import com.decard.exampleSrc.ui.SetupLightEnvironmentActivity;
import com.decard.exampleSrc.ui.SetupTestEnvironmentActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private TextView tv;
    private Button b_open;
    private ImageView imageView;
    private final int MSG_CLEAR_TEXT = 2;
    private final int MSG_APPEND_TEXT = 1;
    private final int MSG_ID_CARD = 11;


    public final int DISCOVERY_CARD_TYPEA = 0;
    public final int DISCOVERY_CARD_TYPEB = 1;
    public final int DISCOVERY_MODE_IDLE_CARD = 0;
    public final int DISCOVERY_MODE_ALL_CARD = 1;
    public final int PICC_BITRATE_TXRX_106K = 0x00;
    public final int PICC_BITRATE_TXRX_212K = 0x01;
    public final int PICC_BITRATE_TXRX_424K = 0x02;
    public final int PICC_BITRATE_TXRX_848K = 0x03;
    public final int PICC_FSD_16 = 0x00;
    public final int PICC_FSD_24 = 0x01;
    public final int PICC_FSD_32 = 0x02;
    public final int PICC_FSD_40 = 0x03;
    public final int PICC_FSD_48 = 0x04;
    public final int PICC_FSD_64 = 0x05;
    public final int PICC_FSD_96 = 0x06;
    public final int PICC_FSD_128 = 0x07;
    public final int PICC_FSD_256 = 0x08;
    public final String desfireATQA = "4403";

    /**
     * execute shell commands
     *
     * @param commands        command array
     * @param isRoot          user root permission
     * @param isNeedResultMsg
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg}
     * is null and {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some
     * excepiton.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        final String COMMAND_SU = "su";
        final String COMMAND_SU_DECARD = "su_decard";
        final String COMMAND_SH = "sh";
        final String COMMAND_EXIT = "exit\n";
        final String COMMAND_LINE_END = "\n";
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;


        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(
                    //isRoot ? COMMAND_SU : COMMAND_SH);
                    isRoot ? COMMAND_SU_DECARD : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    public static class CommandResult {


        /**
         * 运行结果
         **/
        public int result;
        /**
         * 运行成功结果
         **/
        public String successMsg;
        /**
         * 运行失败结果
         **/
        public String errorMsg;


        public CommandResult(int result) {
            this.result = result;
        }


        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

    private boolean turnOnOffLcdBacklight(boolean onOff) {

        String brightnessPath = "/sys/class/leds/lcd-backlight/brightness";
        String cmd = "echo " + (onOff ? 255 : 0) + " > " + brightnessPath;
        CommandResult result = execCommand(new String[]{cmd}, false, true);
        return (result.result == 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        b_open = findViewById(R.id.buttonOpen);
        tv = findViewById(R.id.textView);
        setSupportActionBar(myToolbar);

        //qrCodeThread();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_APPEND_TEXT) {
                //tv.setText((String)msg.obj);
                tv.append((String) msg.obj + "\n");
                tv.setMovementMethod(ScrollingMovementMethod.getInstance());
                int line = tv.getLineCount();
                if (line > 20) {
                    int offset = tv.getLineCount() * tv.getLineHeight();
                    tv.scrollTo(0, offset - tv.getHeight() + tv.getLineHeight());
                }
            } else if (msg.what == MSG_CLEAR_TEXT) {
                clearTextView();
            }
        }
    };

    /**
     * @param s
     * @return
     */
    public String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    private void appendLog(String str1) {
        Message message = handler.obtainMessage();
        message.what = MSG_APPEND_TEXT;
        message.obj = str1;
        handler.sendMessage(message);
    }

    private void myOpr2(int str1) {
        Message message = handler.obtainMessage();
        message.what = str1;
        handler.sendMessage(message);
    }

    private void showIDCard(IDCard idCard) {
        Message message = handler.obtainMessage();
        message.what = MSG_ID_CARD;
        message.obj = idCard;
        handler.sendMessage(message);
    }

    private void clearLog() {
        Message message = handler.obtainMessage();
        message.what = MSG_CLEAR_TEXT;
        handler.sendMessage(message);
    }

    private void clearTextView() {
        tv.setText("");
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.scrollTo(0, 0);
    }

    private void myAddTextview(String str) {
        tv.setText(str);
    }

    private String rndString(int num) {
        String str = "";
        String[] strtemp = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        for (int i = 0; i < num; i++) {
            Random ra = new Random();
            int tempi = ra.nextInt(15);
            str = str + strtemp[tempi];
        }
        return str;
    }

    private int openUSBReader() {
        String port = "AUSB";
        BasicOper.dc_setLanguageEnv(1);
        BasicOper.dc_AUSB_ReqPermission(this);
        int devHandle = BasicOper.dc_open("AUSB", this, "", 0);
        if (devHandle > 0) {
            Log.d("open", "dc_open success devHandle = " + devHandle);
        }
        if (devHandle > 0) {
            appendLog("open port " + port + "success");
            return 0;
        } else {
            appendLog("open port " + port + " error");
            return -2;
        }
    }

    private int openSerialReader() {
        String port = "/dev/dc_spi32765.0";
        String portUart = "/dev/ttyUSB0";
        BasicOper.dc_setLanguageEnv(1);
        int devHandle = BasicOper.dc_open("COM", null, port, 115200);
        if (devHandle < 0) {
            port = portUart;
            devHandle = BasicOper.dc_open("COM", null, port, 115200);
        }
        if (devHandle > 0) {
            appendLog("open port " + port + " success");
            return 0;
        } else {
            appendLog("open port " + port + " error");
            return -2;
        }
    }

    private void closeReader() {
        BasicOper.dc_exit();
        appendLog("close port");
    }

    private int detectMifareCard() {
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                appendLog("dc_reset error");
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                appendLog("dc_config_card error");
                return -1;
            }
            resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                appendLog("dc_card_n_hex success");
                return 0;
            }
        }
    }

    private int insertFelicaCard() {
        final int SYS_SZT = 0x8005;
        final int SRV_SZT = 0x0118;
        final int SYS_OCTOPUS = 0x8008;
        final int SRV_OCTOPUS = 0x0117;
        final int SRV_IDM = 0x220F;
        String[] resultArr;

        // SystemClock.sleep(500);
        /*
        resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_reset error");
            return -1;
        }*/

        resultArr = BasicOper.dc_config_card(3).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_config_card error:" + resultArr[1]);
            // Log.e(TAG,"dc_config_card error:" + resultArr[0] + "|" + resultArr[1]);
            return -1;
        }
        resultArr = BasicOper.dc_FeliCaReset().split("\\|", -1);
        System.out.println(resultArr[0]);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_FeliCaReset error"+resultArr[0]);
            return -1;
        } else {
            appendLog("dc_FeliCaReset success:" + resultArr[2]);
            Log.e(TAG, "dc_FeliCaReset success:" + resultArr[0] + "|" + resultArr[1] + "|" + resultArr[2]);
        }
        FeliCa.Tag tag = new FeliCa.Tag(ByteArrayTools.hexStringToByteArray(resultArr[2]));

        // tag.connect();
        /*--------------------------------------------------------------*/
        // check card system
        /*--------------------------------------------------------------*/

        final int system = tag.getSystemCode();
        appendLog("system code:" + Integer.toHexString(system));
        final FeliCa.ServiceCode service;
        if (system == SYS_OCTOPUS)
            service = new FeliCa.ServiceCode(SRV_OCTOPUS);
        else if (system == SYS_SZT)
            service = new FeliCa.ServiceCode(SRV_SZT);
        else {
            Log.e(TAG, "unknown system code------------:" + Integer.toHexString(system));
            service = new FeliCa.ServiceCode(SRV_IDM);
//            return -1;
        }

        /*--------------------------------------------------------------*/
        // read service data without encryption
        /*--------------------------------------------------------------*/

        final float[] data = new float[]{0, 0, 0};
        final int N = data.length;

        final FeliCa.ReadResponse response1 = tag.requestCode();
        appendLog("RequestCode:" + Util.toHexString(response1.getBlockData()));

        int p = 0;
        for (byte i = 0; p < N; ++i) {
            final FeliCa.ReadResponse r = tag.readWithoutEncryption(service, i);
//            Log.d("FeliCa.ReadResponse", r.getBlockData());
            if (!r.isOkey())
                break;

            data[p++] = (Util.toInt(r.getBlockData(), 0, 4) - 350) / 10.0f;
        }

        tag.close();

        /*--------------------------------------------------------------*/
        // build result string
        /*--------------------------------------------------------------*/

        final String info = parseInfo(tag);
        final String cash = parseBalance(data, p);


        // appendLog("system code:" + Integer.toHexString(system));
        appendLog("name:" + Integer.toHexString(system));
        appendLog("info:" + info);
        appendLog("balance:" + parseBalance(data, p));
        return 0;
    }

    private static String parseInfo(FeliCa.Tag tag) {
        final StringBuilder r = new StringBuilder();
        final String i = "ID:";
        final String p = "PMm";
        r.append(i).append(tag.getIDm().toString());
        r.append("\n");
        r.append(p).append(tag.getPMm().toString());

        return r.toString();
    }

    private static String parseBalance(float[] value, int count) {
        if (count < 1)
            return null;

        final StringBuilder r = new StringBuilder();
        r.append("Balance:");
        for (int i = 0; i < count; ++i)
            r.append(Util.toAmountString(value[i])).append(' ');

        return r.toString();
    }

    private int removeM1Card() {
        appendLog("please remove card>>>>>>>>>>");
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    private void rfFieldOnOff(boolean on) {
        if (on)
            BasicOper.dc_reset();// RF filed on
        else {
            String[] resultArr = BasicOper.dc_reset_close().split("\\|", -1);// RF filed off
            //appendLog("dc_reset_close " + resultArr[0]);
            SystemClock.sleep(50);
        }
    }

    private int detectTypeABCard() {
        while (true) {
            final int ATS = 3;
            rfFieldOnOff(false);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                appendLog("dc_card_n_hex " + "success card sn = " + resultArr[1]);

                resultArr = BasicOper.dc_pro_resethex().split("\\|", -1);
                if (resultArr[0].equals("0000")) {
                    appendLog("dc_pro_resethex " + "success ATR/ATS = " + resultArr[1]);
                    //return 0;
                } else {
                    appendLog("dc_pro_resethex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                    return -13;
                }
                resultArr = BasicOper.dc_GetIso14443Attribute(ATS).split("\\|", -1);
                if (!resultArr[0].equals("0000") || resultArr[1].startsWith("3B")) {
                    appendLog("dc_pro_resethex not CPU card.");
                    return -16;
                }
            } else {
                resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
                if (!resultArr[0].equals("0000")) {
                    return -1;
                }
                resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
                if (resultArr[0].equals("0000")) {
                    appendLog("dc_card_n_hex " + "success card sn = " + resultArr[1]);
                } else {
                    //appendLog("dc_card_n_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                    //appendLog("Test TypeB error");
                    return -12;
                }
            }
            return 0;
        }
    }
    private int detectPiccCpuCard() {
        while (true) {
            final int ATS = 3;
            rfFieldOnOff(false);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                appendLog("dc_card_n_hex " + "success card sn = " + resultArr[1]);
            } else {
                appendLog("dc_card_n_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                appendLog("Test TypeA error");
                return -12;
            }
            resultArr = BasicOper.dc_pro_resethex().split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                appendLog("dc_pro_resethex " + "success ATR/ATS = " + resultArr[1]);
                //return 0;
            } else {
                appendLog("dc_pro_resethex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                return -13;
            }
            resultArr = BasicOper.dc_GetIso14443Attribute(ATS).split("\\|", -1);
            if (!resultArr[0].equals("0000") || resultArr[1].startsWith("3B")) {
                appendLog("dc_pro_resethex not CPU card.");
                return -16;
            }
            return 0;
        }
    }

    private String activateTypeACard(int fsdi, int CID) {
        String[] resultArr = BasicOper.dc_RequestRATS(fsdi, CID).split("\\|", -1);

        if (resultArr[0].equals("0000")) {
            appendLog("dc_RequestRATS " + "success ATS = " + resultArr[1]);
            return resultArr[1];
        } else {
            appendLog("dc_RequestRATS " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return null;
        }
    }

    private int requestPPS(int CID, int DRI, int DSI) {

        String[] resultArr = BasicOper.dc_RequestPPS(CID, DRI, DSI).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            return 0;
        } else {
            return -1;
        }
    }

    private String detectNfcCard() {
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return null;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return null;
            }
            //resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            resultArr = BasicOper.dc_FindCard(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);

            if (resultArr[0].equals("0000")) {
                return resultArr[1];
            } else {
                appendLog("detectNfcCard " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                return null;
            }
        }
    }

    private int removeCPUCard() {
        appendLog("please remove card>>>>>>>>>>");
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    private int detectIDCard() {
        appendLog("Please Present ID card<<<<<<<<<<<<<<<<<<<<<\n");
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_get_idsnr().split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    private int removeIDCard() {
        appendLog("please Remove ID card>>>>>>>>>>>>>>>>>>");
        while (true) {
            SystemClock.sleep(500);
            String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;

            }
            resultArr = BasicOper.dc_get_idsnr().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    private int testM1() {
        int keyAB = 0;// 0:KeyA 4:KeyB
        int sector = 1;
        int blockIndex = sector * 4 + 0;

        String key = "FFFFFFFFFFFF";

        appendLog("Test M1 Card.............");
        SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_reset " + "success");
        } else {
            appendLog("dc_reset " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("M1 test error");
            return -3;
        }
        resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_config_card " + " success ");
        } else {
            appendLog("dc_config_card " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("M1 test error");
            return -4;
        }
        resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_card_n_hex " + "success card sn = " + resultArr[1]);
            appendLog("card sn:" + resultArr[1]);
        } else {
            appendLog("dc_card_n_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("M1 test error");
            return -5;
        }
        resultArr = BasicOper.dc_authentication_pass(keyAB, sector, key).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_authentication_pass success");
        } else {
            appendLog("dc_authentication_pass " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("M1 test error");
            return -6;
        }

        String str1 = rndString(32);
        resultArr = BasicOper.dc_write_hex(blockIndex, str1).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_write_hex " + "success");
            appendLog("write data:" + str1);
        } else {
            appendLog("dc_write_hex " + "error code = " + resultArr[0]);
            appendLog("M1 test error");
            return -8;
        }
        resultArr = BasicOper.dc_read_hex(blockIndex).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_read_hex " + "success data : " + resultArr[1]);
        } else {
            appendLog("dc_read_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("M1 test error");
            return -9;
        }
        appendLog("M1 test success");
        return 0;
    }

    private String[] getPICCAtrribute() {
        final int ATQA = 1;
        final int SAK = 2;
        final int ATS = 3;
        String attribute[] = new String[]{"", "", ""};
        String[] resultArr = BasicOper.dc_GetIso14443Attribute(ATQA).split("\\|", -1);

        if (resultArr[0].equals("0000")) {
            appendLog("dc_GetIso14443Attribute " + "success atqa:" + resultArr[1]);
            attribute[0] = resultArr[1];
        } else {
            appendLog("dc_GetIso14443Attribute " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return attribute;
        }
        resultArr = BasicOper.dc_GetIso14443Attribute(SAK).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_GetIso14443Attribute " + "success sak:" + resultArr[1]);
            attribute[1] = resultArr[1];
        } else {
            appendLog("dc_GetIso14443Attribute " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return attribute;
        }
        resultArr = BasicOper.dc_GetIso14443Attribute(ATS).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_GetIso14443Attribute " + "success ats:" + resultArr[1]);
            attribute[2] = resultArr[1];
        } else {
            appendLog("dc_GetIso14443Attribute " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return attribute;
        }
        return attribute;
    }

    private int testTypeACpu() {
        String apdu = "0084000008";
        //String apdu = "00a4040010xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx00";

        appendLog("Test TypeA Card...................");
        // SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_reset " + "success");
        } else {
            appendLog("dc_reset " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("Test TypeA error");
            return -10;
        }
        resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEA).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_config_card " + "success ");
        } else {
            appendLog("dc_config_card " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("Test TypeA error");
            return -11;
        }
        resultArr = BasicOper.dc_card_n_hex(DISCOVERY_MODE_ALL_CARD).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_card_n_hex " + "success card sn = " + resultArr[1]);
        } else {
            appendLog("dc_card_n_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("Test TypeA error");
            return -12;
        }

        resultArr = BasicOper.dc_pro_resethex().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_pro_resethex " + "success ATR/ATS = " + resultArr[1]);
        } else {
            appendLog("dc_pro_resethex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -13;
        }
        String[] atrribute = getPICCAtrribute();

        if (resultArr[1].startsWith("3B")) {
            appendLog("dc_pro_resethex not CPU card.");
            return -16;
        }

        appendLog("send apdu:" + apdu);
        resultArr = BasicOper.dc_pro_commandhex(apdu, 7).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_pro_commandhex " + "success reponse apdu = " + resultArr[1]);
        } else {
            appendLog("dc_pro_commandhex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -14;
        }
        resultArr = BasicOper.dc_pro_halt().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_pro_halt " + "success");
        } else {
            appendLog("dc_pro_halt " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -15;
        }
        return 0;
    }

    int TSAM(int samSlot) throws Exception {
        String apdu = "0084000008";
        String rwSamNumber = "";
        String[] resultArr;
        Sam sam = new Sam(samSlot);
        if(sam.initSam().equals("0000")){
            appendLog("dc_setcpu success");
        }
        else {
            appendLog("dc_setcpu error ");
            return -1;
        }
        if(sam.resetSam().equals("0000")){
            appendLog("dc_setcpu success");
        } else{
            appendLog("sam reset error");
            return -2;
        }
        String samNormalModeData = sam.setToNormalMode();
        if (samNormalModeData!=null){
            appendLog("sam set to normal mode successfully---"+samNormalModeData);
        }else {
            appendLog("sam can not set to normal mode");
            return -4;
        }
        String samAttention = sam.sendAttention();
        if (samAttention!=null){
            appendLog("sam attention send successfully---"+samAttention);
        }else {
            appendLog("sam can not send attention");
            return -5;
        }
        String samAuth1 = sam.sendAuth1();
        if (samAuth1!=null){
            appendLog("sam auth1 send successfully---"+samAuth1);
        }else {
            appendLog("sam can not send auth1");
            return -6;
        }
        String samAuth1Result = sam.checkAuth1Result(samAuth1);
        if (samAuth1Result!=null){
            appendLog("sam auth1 result check successfully---"+samAuth1Result);
        }else {
            appendLog("sam auth1 result check failed");
            return -7;
        }
        String samAuth2 = sam.sendAuth2();
        if (samAuth2!=null){
            appendLog("sam auth2 send successfully---"+samAuth2);
        }else {
            appendLog("sam can not send auth2");
            return -8;
        }
        String samAuth2Result = sam.checkAuth2Result(samAuth2);
        if (samAuth2Result!=null){
            appendLog("sam auth2 result check successfully---"+samAuth2Result);
        }else {
            appendLog("sam auth2 result check failed");
            return -9;
        }
        FelicaCard felicaCard = new FelicaCard(sam);
        String res = felicaCard.detectFelicaCard();
        if(!TextUtils.isEmpty(res)){
            appendLog("Felica card detect successful---->"+res);
            appendLog("IDm---->"+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getIDm()));
            appendLog("PMm---->"+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getPMm()));
            appendLog("SystemCode---->"+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getSystemCode()));
        } else{
            appendLog("Felica card detect failed");
            return -10;
        }
        int i = felicaCard.iWaitForAndAnalyzeFeliCa();
        if(i!=0){
            appendLog("Felica card analyze successfully");
            appendLog("Card control code :"+felicaCard.getFelicaCardDetail().getAttributeInfo().getBinCardControlCode());
            appendLog("txnid :"+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getFelicaCardDetail().getAttributeInfo().getBinTxnDataId()));
            appendLog("name: "+new String(felicaCard.getFelicaCardDetail().getPersonalInfo().getBinName()));
            appendLog("phone: "+new String(felicaCard.getFelicaCardDetail().getPersonalInfo().getBinPhone()));
            appendLog("birth day: "+new String(felicaCard.getFelicaCardDetail().getPersonalInfo().getBinBirthday()));
            appendLog("reserved: "+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getFelicaCardDetail().getPersonalInfo().getBinReserved()));
            appendLog("getLngRemainingSV in hex: "+ com.decard.exampleSrc.Utils.byteToHex(felicaCard.getFelicaCardDetail().getEPurseInfo().getBinRemainingSV()));
            appendLog("getLngRemainingSV: "+felicaCard.getFelicaCardDetail().getGeneralInfo().getLngRemainingSV());
            appendLog("getIntNegativeValue: "+felicaCard.getFelicaCardDetail().getGeneralInfo().getIntNegativeValue());
            appendLog("getLngCashBackData: "+felicaCard.getFelicaCardDetail().getGeneralInfo().getLngCashBackData());
            appendLog("rapidpass card type: "+felicaCard.getFelicaCardDetail().getGeneralInfo().getBinCardType());
        } else{
            appendLog("Can not analyze Felica card");
            return -11;
        }
        appendLog("Trying to update balance");
        String r = felicaCard.readOpenBlock();
        if(TextUtils.isEmpty(r)){
            appendLog("Can not read open block");
            return -11;
        } else {
            Log.d("open block data", r);
            appendLog("open block data :"+r);
        }


        /*i = felicaCard.updateBalance();
        if(i==0){
            appendLog("update balance failed");
            return -12;
        }
        appendLog("update balance successful (probably)");*/

        /*resultArr = BasicOper.dc_setcpu(samSlot).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_setcpu success");
        } else {
            appendLog("dc_setcpu error " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -1;
        }
        resultArr = BasicOper.dc_cpureset_hex().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_cpureset_hex success," + "ATR/ATS = " + resultArr[1]);
        } else {
            appendLog("dc_cpureset_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -2;
        }*/



        /*resultArr = BasicOper.dc_cpuapdu_hex(apdu).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_cpuapdu_hex " + "success response = " + resultArr[1]);
        } else {
            appendLog("dc_cpuapdu_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -2;
        }*/
        resultArr = BasicOper.dc_cpudown().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_cpudown " + "success");
        } else {
            appendLog("dc_cpudown " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            return -3;
        }
        return 0;
    }

    int TestTypeB() {
        String apdu = "0084000008";
        appendLog("Test TypeB...................");
        SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_reset " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -1;
        }
        resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_config_card " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -11;
        }
        resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_request_b_hex " + "success card sn = " + resultArr[1]);
        } else {
            appendLog("dc_request_b_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("Teset TypeB error");
            return -12;
        }
        appendLog("send apdu:" + apdu);
        resultArr = BasicOper.dc_procommandInt_hex(apdu, 7).split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_pro_commandhex " + "success reponse apdu = " + resultArr[1]);
            appendLog("reponse apdu:" + resultArr[1]);
        } else {
            appendLog("dc_pro_commandhex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -14;
        }
        resultArr = BasicOper.dc_pro_halt().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            appendLog("dc_pro_halt " + "success");
        } else {
            appendLog("dc_pro_halt " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -15;
        }
        resultArr = BasicOper.dc_reset_close().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
           // appendLog("dc_reset_close success");
        } else {
            //appendLog("dc_reset_close " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -16;
        }
        appendLog("TypeB test success");
        return 0;
    }

    int removeTypeBCard() {
        SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_reset " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("TypeB test error");
            return -10;
        }
        resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            return -1;
        }
        resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            return 0;
        }
        appendLog("Please Remove TypeB card>>>>>>");
        while (true) {
            SystemClock.sleep(500);
            resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    int detectTypeBCard() {
        SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_reset().split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_reset " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            appendLog("Test TypeB error");
            return -10;
        }
        resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            return 0;
        }
        resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            return 0;
        }
        appendLog("Please Present TypeB card<<<<<<<<<<<<<<<<<");
        while (true) {
            SystemClock.sleep(500);
            resultArr = BasicOper.dc_reset().split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_config_card(DISCOVERY_CARD_TYPEB).split("\\|", -1);
            if (!resultArr[0].equals("0000")) {
                return -1;
            }
            resultArr = BasicOper.dc_card_b_hex().split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                return 0;
            }
        }
    }

    public void onSam1(View v) throws Exception {
        clearLog();
        appendLog("Test SAM1 card.........");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int st = 0;
                try {
                    st = TSAM(3);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (st != 0) {
                    appendLog("test SAM1 card error");
                    return;
                }
                appendLog("test SAM1 card success");
            }
        }).start();
    }

    public void onM1(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearLog();
                appendLog("Test M1 Card....................");
                appendLog("please Present M1 card>>>>>>>>>>");
                int st = detectMifareCard();
                if (st != 0) {
                    appendLog("test M1 error");
                    return;
                }
                appendLog("test M1 card..............");
                st = testM1();
                if (st != 0) {
                    appendLog("M1 card test error");
                    return;
                }
                appendLog("please Remove M1 card>>>>>>>>>");
                st = removeM1Card();
                if (st != 0) {
                    appendLog("test M1 error");
                    return;
                }
                appendLog("test M1 success");
            }
        }).start();
    }

    public void onLed(View v) {
        appendLog("Test LED.............");
        SystemClock.sleep(500);
        String[] resultArr = BasicOper.dc_ctlled(1, 0).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        resultArr = BasicOper.dc_ctlled(2, 0).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        resultArr = BasicOper.dc_ctlled(3, 0).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        resultArr = BasicOper.dc_ctlled(4, 0).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        SystemClock.sleep(500);
        resultArr = BasicOper.dc_ctlled(1, 1).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        SystemClock.sleep(500);
        resultArr = BasicOper.dc_ctlled(2, 1).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        SystemClock.sleep(500);
        resultArr = BasicOper.dc_ctlled(3, 1).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        SystemClock.sleep(500);
        resultArr = BasicOper.dc_ctlled(4, 1).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("dc_ctlled" + " error");
        }
        appendLog("Test LED success");
    }

    public void test_sam_av2() {
        int psamSlot = 0;
        int keyNumber = 0;
        int baud = 115200;
        byte[] sam_key = {
                (byte) 0x70, (byte) 0x64, (byte) 0xE2, (byte) 0x1A, (byte) 0xA4, (byte) 0xFE, (byte) 0x40, (byte) 0xE2,
                (byte) 0x37, (byte) 0x55, (byte) 0x44, (byte) 0xED, (byte) 0x70, (byte) 0x2E, (byte) 0xB0, (byte) 0x0B
        };
        int keyLen = 256;
        clearLog();
        appendLog("sam av2 use psam slot SAM1");
        BasicOper.dc_exit();
        int st = openUSBReader();
        if (st < 0) {
            st = openSerialReader();
            if (st >= 0) {
                psamSlot = 2;
            }
        } else {
            psamSlot = 0;
        }
        byte[] rpdu;
        String key = null;
        boolean auth_result = false;

        SAMP18Q samP18Q = new SAMP18Q(psamSlot);
        rpdu = samP18Q.connectSAM();
        if (rpdu != null) {
            auth_result = samP18Q.samAV2_authHost(sam_key, keyNumber);
            appendLog("samAV2_authHost:" + auth_result);
        } else {
            appendLog("sam " + psamSlot + " reset error");
        }

        if (auth_result) {
            rpdu = samP18Q.samAV2_GenerateKeyPair(keyNumber, keyLen);
            key = ByteArrayTools.toHexString(rpdu, true);
            //samP18Q.samAV2_PKI_ExportPrivateKey(keyNumber);
        }
        samP18Q.disconnectSAM();

        appendLog("samAV2_GenerateKeyPair:" + key);
        if (key != null && key.endsWith("9000"))
            appendLog("SAM AV2 success");
        else
            appendLog("SAM AV2 failed");
    }

    private int[] PICC_ParseRate(byte[] ats) {
        int DRI = PICC_BITRATE_TXRX_106K;
        int DSI = PICC_BITRATE_TXRX_106K;
        byte TA1 = (byte) 0x00;
        if ((ats[1] & 0x40) == 0x40)
            TA1 = ats[2];
        if (0 == TA1) {
            return new int[]{DRI, DSI};
        }
        /*PCD->PICC*/
        if (0x01 == (TA1 & 0x01)) {
            DRI = PICC_BITRATE_TXRX_212K;
        }
        if (0x02 == (TA1 & 0x02)) {
            DRI = PICC_BITRATE_TXRX_424K;
        }
        if (0x04 == (TA1 & 0x04)) {
            DRI = PICC_BITRATE_TXRX_848K;
        }

        /*PICC->PCD*/
        if (0x10 == (TA1 & 0x10)) {
            DSI = PICC_BITRATE_TXRX_212K;
        }
        if (0x20 == (TA1 & 0x20)) {
            DSI = PICC_BITRATE_TXRX_424K;
        }
        if (0x40 == (TA1 & 0x40)) {
            DSI = PICC_BITRATE_TXRX_848K;
        }

        if (DSI != DRI) {
            if (DSI > DRI) {
                DSI = DRI;
            } else {
                DRI = DSI;
            }
        }
        return new int[]{DRI, DSI};
    }

    public void test_MifarePlus() {
        int CID = 0;
        int fsdi = PICC_FSD_256;// FSD = 256 bytes
        int DRI = PICC_BITRATE_TXRX_106K;// receive bitrate
        int DSI = PICC_BITRATE_TXRX_106K;// send bitrate
        int st;
        clearLog();
        appendLog("please Present MifarePlus card>>>>>>>>>>>>>>>");
        try {
            String uid = detectNfcCard();
            if (uid == null) {
                appendLog("test MifarePlus error");
                return;
            } else {
                appendLog("detectNfcCard " + "success uid = " + uid);
            }
            // uid sak and atqa could be returned after card detected
            String[] atrribute = getPICCAtrribute();

            String ATS = activateTypeACard(fsdi, CID);
            if (ATS == null) {
                appendLog("activateTypeACard error");
                return;
            } else {
                appendLog("activateTypeACard success ");
            }
            if (ATS != null && ATS.length() > 2) {
                byte[] ats = ByteArrayTools.hexStringToByteArray(ATS);
                int DRI_DSI[] = PICC_ParseRate(ats);

                DRI = DRI_DSI[0];
                DSI = DRI_DSI[1];
                appendLog("==>to requestPPS DRI =" + DRI + " DSI = " + DSI);
                st = requestPPS(CID, DRI, DSI);

                if (st != 0) {
                    appendLog("requestPPS error");
                    return;
                } else {
                    appendLog("requestPPS success");
                }
            }
            // sak ,atqa, ats could be returned.
            getPICCAtrribute();

            byte[] authKey = new byte[16];

            for (int i = 0; i < authKey.length; i++)
                authKey[i] = (byte) 0xff;

            P18QMifarePlus channel = new P18QMifarePlus();
            MifarePlus mifarePlus = new MifarePlus(channel);

            /*
            st = mifarePlus.mplus_AuthKey(0,authKey);

            if(st == 0){
                appendLog("test MifarePlus success");
            }else{
                appendLog("test MifarePlus fail");
                return;
            }
            */
            // not succeed now.
            byte[] rpdu = mifarePlus.mplus_plainRead(0, 1);

            if (rpdu != null) {
                appendLog("MifarePlus mplus_plainRead success:" + Utils.getHexString(rpdu));
            } else {
                appendLog("MifarePlus mplus_plainRead fail");
            }

            st = removeCPUCard();
            if (st != 0) {
                appendLog("MifarePlus test error");
            } else {
                appendLog("MifarePlus test Success");
            }
        } catch (Exception e) {
            appendLog("MifarePlus test fail:" + e.toString());
        }
    }

    private String getEVType(VersionInfo versionInfo) {
        int hardwareType = versionInfo.getHardwareType();
        int hardwareVersion = versionInfo.getHardwareVersionMajor();
        if (hardwareType == 1) {
            switch (hardwareVersion) {
                case 01:
                    return "DESFIRE EV1";
                case 18:
                    return "DESFIRE EV2";
                case 51:
                    return "DESFIRE EV3";
            }
        }
        return "Unknown";
    }

    public boolean discoverEVCard(){
        int st = detectPiccCpuCard();
        if (st != 0) {
            appendLog("Test DesFireEV1 error");
            return false;
        }
        // uid sak and atqa could be returned after card detected
        String[] atrribute = getPICCAtrribute();
        if (!atrribute[0].equals(desfireATQA)) {
            appendLog("Not DesFireEV2 card");
            appendLog("Test DesFireEV2 error");
            return false;
        }
        return true;
    }
    public void test_DesfireEV1() {
        int CID = 0;
        int fsdi = PICC_FSD_256;// FSD = 256 bytes
        int DRI = PICC_BITRATE_TXRX_106K;// receive bitrate
        int DSI = PICC_BITRATE_TXRX_106K;// send bitrate
        int st;
        clearLog();
        appendLog("please Present DesFireEV1 card>>>>>>>>>>>>>>>");
        try {
            st = detectPiccCpuCard();
            if (st != 0) {
                appendLog("Test DesFireEV1 error");
                return;
            }
            // uid sak and atqa could be returned after card detected
            String[] atrribute = getPICCAtrribute();
            if (!atrribute[0].equals(desfireATQA)) {
                appendLog("Not DesFireEV2 card");
                appendLog("Test DesFireEV2 error");
                return;
            }

            byte[] authKey = new byte[16];

            for (int i = 0; i < authKey.length; i++)
                authKey[i] = (byte) 0xff;

            DESFireEV3 desFireEV3 = new DESFireEV3(new P18QDesfireEV());
            try {
                VersionInfo versionInfo = desFireEV3.getVersion();
                appendLog("DesFireEV2 VendorId:" + versionInfo.getHardwareVendorId());
                appendLog("DesFireEV2 Total Memory:" + versionInfo.getHardwareStorageSize());
                appendLog("DesFireEV2 CardType:" + getEVType(versionInfo));

                // EV1 ((hardwareType == 1) && (hardwareVersion == 01));

                byte aid[] = new byte[]{0x33, 0x44, 0x55};
                byte amks = 0x01;

                DesfireKeyType keyType = DesfireKeyType.DES;
                byte numberOfKeys = 3;
                // desFireEV1.deleteApplication(aid);
                boolean result = desFireEV3.createApplication(aid, amks, keyType, numberOfKeys);

                if (result) {
                    appendLog("DesFireEV1 createApplication success");
                } else {
                    appendLog("DesFireEV1 createApplication fail");
                }
                desFireEV3.selectApplicationByAid(DESFireEV3.MASTER_APPLICATION_IDENTIFIER);
                List<byte[]> idsList = desFireEV3.getApplicationIdsList();
                if (idsList != null) {
                    for (int i = 0; i < idsList.size(); i++) {
                        appendLog("DesFireEV1 getApplicationID:" + ByteArrayTools.toHexString(idsList.get(i), true));
                    }
                }
                byte[] dfNames = desFireEV3.getApplicationDfNames();

            } catch (Exception e) {
                appendLog("DesFireEV1 createApplication fail " + e.getMessage());
            }

            st = removeCPUCard();
            if (st != 0) {
                appendLog("DesFireEV1 test error");
            } else {
                appendLog("DesFireEV1 test Success");
            }
        } catch (Exception e) {
            appendLog("DesFireEV1 test fail:" + e.toString());
        }
    }

    public void test_DesfireEV2() {
        int CID = 0;
        int fsdi = PICC_FSD_256;// FSD = 256 bytes
        int DRI = PICC_BITRATE_TXRX_106K;// receive bitrate
        int DSI = PICC_BITRATE_TXRX_106K;// send bitrate
        int st;
        clearLog();
        appendLog("please Present DesFireEV2 card>>>>>>>>>>>>>>>");
        try {
            st = detectPiccCpuCard();
            if (st != 0) {
                appendLog("Test DesFireEV2 error");
                return;
            }
            // uid sak and atqa could be returned after card detected
            String[] atrribute = getPICCAtrribute();
            if (!atrribute[0].equals(desfireATQA)) {
                appendLog("Not DesFireEV2 card");
                appendLog("Test DesFireEV2 error");
                return;
            }
            DESFireEV3 desFireEV3 = new DESFireEV3(new P18QDesfireEV());
            try {
                VersionInfo versionInfo = desFireEV3.getVersion();
                if (versionInfo != null) {
                    appendLog("DesFireEV2 VendorId:" + versionInfo.getHardwareVendorId());
                    appendLog("DesFireEV2 Total Memory:" + versionInfo.getHardwareStorageSize());
                    appendLog("DesFireEV2 CardType:" + getEVType(versionInfo));
                }
                boolean result = desFireEV3.formatPicc(0);
                if (result) {
                    appendLog("DesFireEV2 formatPicc Success");
                }

                desFireEV3.selectApplicationByAid(DESFireEV3.MASTER_APPLICATION_IDENTIFIER);
                desFireEV3.getApplicationIdsList();
                byte[] dfNames = desFireEV3.getApplicationDfNames();


            } catch (Exception e) {
                appendLog("DesFireEV2 test fail " + e.getMessage());
            }

            st = removeCPUCard();
            if (st != 0) {
                appendLog("DesFireEV2 test error");
            } else {
                appendLog("DesFireEV2 test Success");
            }
        } catch (Exception e) {
            appendLog("DesFireEV2 test fail:" + e.toString());
        }
    }

    public void test_DesfireEV3() {
        int CID = 0;
        int fsdi = PICC_FSD_256;// FSD = 256 bytes
        int DRI = PICC_BITRATE_TXRX_106K;// receive bitrate
        int DSI = PICC_BITRATE_TXRX_106K;// send bitrate
        int st;
        clearLog();
        appendLog("please Present DesFireEV3 card>>>>>>>>>>>>>>>");
        try {
            st = detectPiccCpuCard();
            if (st != 0) {
                appendLog("Test DesFireEV3 error");
                return;
            }
            // uid sak and atqa could be returned after card detected
            String[] atrribute = getPICCAtrribute();
            if (!atrribute[0].equals(desfireATQA)) {
                appendLog("Not DesFireEV2 card");
                appendLog("Test DesFireEV2 error");
                return;
            }

            byte[] authKey = new byte[16];

            for (int i = 0; i < authKey.length; i++)
                authKey[i] = (byte) 0xff;

            DESFireEV3 desFireEV3 = new DESFireEV3(new P18QDesfireEV());

            try {
                VersionInfo versionInfo = desFireEV3.getVersion();
                if (versionInfo != null) {
                    appendLog("DesFireEV2 VendorId:" + versionInfo.getHardwareVendorId());
                    appendLog("DesFireEV2 Total Memory:" + versionInfo.getHardwareStorageSize());
                    appendLog("DesFireEV2 CardType:" + getEVType(versionInfo));
                }
                boolean result = desFireEV3.test();
                desFireEV3.selectApplicationByAid(DESFireEV3.MASTER_APPLICATION_IDENTIFIER);

                result = desFireEV3.getApplicationsIsoData();
                if (result) {
                    appendLog("DesFireEV3 test success");
                } else {
                    appendLog("DesFireEV3 test fail");
                }
            } catch (Exception e) {
                appendLog("DesFireEV3 test fail " + e.getMessage());
            }

            st = removeCPUCard();
            if (st != 0) {
                appendLog("DesFireEV3 test error");
            } else {
                appendLog("DesFireEV test Success");
            }
        } catch (Exception e) {
            appendLog("DesFireEV test fail:" + e.toString());
        }
    }

    public void test_Felica() {
        String apdu = "";
        clearLog();
        appendLog("please Present Felica card>>>>>>>>>>>>>>>");
        try {
            int st = insertFelicaCard();
            if (st != 0) {
                appendLog("test Felica error");
                return;
            }
        } catch (Exception e) {

        }
    }

    public void onSamAv2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                test_sam_av2();
            }
        }).start();
    }

    public void onMifarePlus(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                test_MifarePlus();
            }
        }).start();
    }

    public void onDesfireEV1(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                test_DesfireEV1();
            }
        }).start();
    }

    public void onDesfireEV2(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                test_DesfireEV2();
            }
        }).start();
    }

    public void test_DesfireEVFull() {
        int st;
        clearLog();
        appendLog("please Present DesFireEV3 card>>>>>>>>>>>>>>>");
        try {
            st = detectPiccCpuCard();
            if (st != 0) {
                appendLog("Test DesFireEV3 error");
                return;
            }
            // uid sak and atqa could be returned after card detected
            String[] atrribute = getPICCAtrribute();
            if (!atrribute[0].equals(desfireATQA)) {
                appendLog("Not DesFireEV2 card");
                appendLog("Test DesFireEV2 error");
                return;
            }
            Intent intent = new Intent(MainActivity.this, DesfireMainActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            appendLog("DesFireEV test fail:" + e.toString());
        }
    }


    public void onDesfireEV3(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                test_DesfireEV3();
            }
        }).start();
    }

    public void onDesfireEVFull(View v) {

        Intent intent = new Intent(MainActivity.this, DesfireMainActivity.class);
        startActivity(intent);
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                test_DesfireEVFull();
            }
        });
         */

    }

    public void onFelica(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                test_Felica();
            }
        }).start();
    }

    public int openReader() {
        int st = openUSBReader();
        if (st < 0) {
            st = openSerialReader();
        }
        supportInvalidateOptionsMenu();
        return st;
    }

    private String getReaderVersion() {
        String[] resultArr = BasicOper.dc_getver().split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            return resultArr[1];
        } else {
            return "";
        }
    }
    private boolean isReaderOpened(){
        return b_open.getText().toString().equals("CloseReader");
    }
    public void onOpenReader(View v) {
        clearLog();
        String str = b_open.getText().toString();
        if (str.equals("OpenReader")) {
            int st = openReader();
            //int st = openSerialReader();
            if (st == 0) {
                b_open.setText("CloseReader");
                appendLog("Reader Version:" + getReaderVersion());
            }
        } else {
            closeReader();
            b_open.setText("OpenReader");
        }
    }

    public void onTypeA(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearLog();
                appendLog("please Present TypeA card>>>>>>>>>>>>>>>");
                int st = detectMifareCard();
                if (st != 0) {
                    appendLog("Test TypeA error");
                    return;
                }
                appendLog("Test TypeA Card.............");
                st = testTypeACpu();
                if (st != 0) {
                    appendLog("Test TypeA error");
                    return;
                }
                appendLog("please Remove TypeA card");
                st = removeM1Card();
                if (st != 0) {
                    appendLog("Test TypeA error");
                } else {
                    appendLog("Test TypeA Success");
                }
            }
        }).start();
    }

    public void onTypeB(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearLog();
                int st = -1;
                appendLog("Test TypeB Card....................");
                appendLog("Please Present TypeB Card");
                st = detectTypeBCard();
                if (st != 0) {
                    appendLog("Test TypeB Card Error");
                    return;
                }
                appendLog("Test TypeB Card..............");
                st = TestTypeB();
                if (st != 0) {
                    appendLog("Test TypeB Card error");
                    return;
                }
                appendLog("Please Remove TypeB card>>>>>>>>>>>>>");
                st = removeTypeBCard();
                if (st != 0) {
                    appendLog("Test TypeB Card error");
                    return;
                }
                appendLog("Test TypeB Card success");
            }
        }).start();
    }
    private void qrCodeThread(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                    String[] resultArr = BasicOper.dc_Scan2DBarcodeStart(0).split("\\|", -1);
                    if (!resultArr[0].equals("0000")) {
                        appendLog("Test QRcode error");
                    }
                    appendLog("Please scan QRcode...............");

                while (true) {
                    SystemClock.sleep(50);
                    resultArr = BasicOper.dc_Scan2DBarcodeGetData().split("\\|", -1);
                    if (resultArr[0].equals("0000")) {
                        appendLog(hexStringToString(resultArr[1]));
                    }
                }
                /*
                if(false) {
                    resultArr = BasicOper.dc_Scan2DBarcodeExit().split("\\|", -1);
                    if (!resultArr[0].equals("0000")) {
                        appendLog("Test QRcode error");
                    }
                    appendLog("Test QRcode success");
                }
                */
            }
        }).start();
    }
    public void onQRcode(View v) {
        clearLog();
        appendLog("Test QRcode..............");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] resultArr = BasicOper.dc_Scan2DBarcodeStart(0).split("\\|", -1);
                if (!resultArr[0].equals("0000")) {
                    appendLog("Test QRcode error");
                    return;
                }
                appendLog("Please scan QRcode...............");
                while (true) {
                    SystemClock.sleep(500);
                    resultArr = BasicOper.dc_Scan2DBarcodeGetData().split("\\|", -1);
                    if (resultArr[0].equals("0000")) {
                        appendLog(hexStringToString(resultArr[1]));
                        break;
                    }
                }
                resultArr = BasicOper.dc_Scan2DBarcodeExit().split("\\|", -1);
                if (!resultArr[0].equals("0000")) {
                    appendLog("Test QRcode error");
                    return;
                }
                appendLog("Test QRcode success");
            }
        }).start();
    }

    public void onSeg(View v) {
        clearLog();
        String[] resultArr = BasicOper.dc_LedDisplay(1, 0, 1).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("seg display error");
        }
        resultArr = BasicOper.dc_LedDisplay(2, 0, 2).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("seg display error");
        }
        resultArr = BasicOper.dc_LedDisplay(3, 0, 3).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("seg display error");
        }
        resultArr = BasicOper.dc_LedDisplay(4, 0, 4).split("\\|", -1);
        if (!resultArr[0].equals("0000")) {
            appendLog("seg display error");
        }
        appendLog("seg display success");
    }

    public void onQRcodeTypeAB(View v) {
        String apdu = "0084000008";
        clearLog();
        appendLog("Test QRcode..............");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] resultArr = BasicOper.dc_Scan2DBarcodeStart(0).split("\\|", -1);
                if (!resultArr[0].equals("0000")) {
                    appendLog("Test QRcode error");
                    return;
                }
                appendLog("Please scan QRcode...............");
                while (true) {
                    //SystemClock.sleep(500);
                    int st = detectTypeABCard();
                    if(st == 0){
                        appendLog("send apdu:" + apdu);
                        resultArr = BasicOper.dc_pro_commandhex(apdu, 7).split("\\|", -1);
                        if (resultArr[0].equals("0000")) {
                            appendLog("dc_pro_commandhex " + "success reponse apdu = " + resultArr[1]);
                        } else {
                            appendLog("dc_pro_commandhex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                            //  return -14;
                        }
                        resultArr = BasicOper.dc_pro_halt().split("\\|", -1);
                        if (resultArr[0].equals("0000")) {
                            appendLog("dc_pro_halt " + "success");
                        } else {
                            appendLog("dc_pro_halt " + "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
                            // return -15;
                        }
                    }else{
                        resultArr = BasicOper.dc_Scan2DBarcodeGetData().split("\\|", -1);
                        if (resultArr[0].equals("0000")) {
                            appendLog(hexStringToString(resultArr[1]));
                            break;
                        }
                    }
                }
                resultArr = BasicOper.dc_Scan2DBarcodeExit().split("\\|", -1);
                if (!resultArr[0].equals("0000")) {
                    appendLog("Test QRcode error");
                    return;
                }
                appendLog("Test QRcode success");
            }
        }).start();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mFormatPicc = menu.findItem(R.id.action_desfireEV);
        mFormatPicc.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, DesfireMainActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    */

    /**
     * section for options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isReaderOpened()){
            return false;
        }
        getMenuInflater().inflate(R.menu.menu_desfire_activity_main, menu);

        MenuItem mFormatPicc = menu.findItem(R.id.action_format_picc);
        mFormatPicc.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, FormatPiccActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mSetupTestEnvironment = menu.findItem(R.id.action_setup_test_environment);
        mSetupTestEnvironment.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SetupTestEnvironmentActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mPersonalize = menu.findItem(R.id.action_personalize);
        mPersonalize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, PersonalizeActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mCreateApplication = menu.findItem(R.id.action_create_application);
        mCreateApplication.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, CreateApplicationActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mCreateFile = menu.findItem(R.id.action_create_file);
        mCreateFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, CreateFileActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mSetupLightEnvironment = menu.findItem(R.id.action_setup_light_environment);
        mSetupLightEnvironment.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SetupLightEnvironmentActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExportTextFile = menu.findItem(R.id.action_export_text_file);
        mExportTextFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportTextFile");
               // exportTextFile();
                return false;
            }
        });
        MenuItem mDesFireMain = menu.findItem(R.id.action_desfire_main);
        mDesFireMain.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, DesfireMainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        MenuItem mGoToHome = menu.findItem(R.id.action_return_main);
        mGoToHome.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onDestroy(){
        Log.i(TAG,"======onDestroy ==========");
        super.onDestroy();
    }
}
package com.newboston.nfctut;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;


public class WriteActivity extends ActionBarActivity {

    NfcAdapter nfcAdapter;
    EditText tagTxtContent;
    Button clickToRead;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter!=null && nfcAdapter.isEnabled())
        {
            Toast.makeText(this, "NFC is available :)", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "NFC not available :(", Toast.LENGTH_LONG).show();
        }


        tagTxtContent = (EditText)findViewById(R.id.tagTxtContent);
        clickToRead = (Button)findViewById(R.id.clickToRead);
        context = getApplicationContext();

        clickToRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent read = new Intent(WriteActivity.this, ReadActivity.class);
                startActivity(read);
            }
        });

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDiscovered = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        /*try
        {
            tagDiscovered.addDataType("application/" + context.getPackageName());
        }
        catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("fail", e);
        }*/

        intentFilters = new IntentFilter[]{tagDiscovered};
    }


    @Override
    protected void onNewIntent(Intent intent) {

        /*if (intent.hasExtra(nfcAdapter.EXTRA_TAG))
        {*/
            //Toast.makeText(this, "NFC intent", Toast.LENGTH_LONG).show();


            //if (toggleButton.isChecked())//read
            /*{
                Parcelable [] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (parcelables != null && parcelables.length>0)
                {
                    readTextFromMessage((NdefMessage) parcelables[0]);
                }
                else
                {
                    Toast.makeText(this, "No NDEF message found", Toast.LENGTH_LONG).show();
                }

            }*/
            //else //write
            //{
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                NdefMessage ndefMessage = createNdefMessage(tagTxtContent.getText()+"");

                writeNdefMessage(tag, ndefMessage);
            //}
        //}

        super.onNewIntent(intent);
    }

    /*private void readTextFromMessage(NdefMessage ndefMessage)
    {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length>0)
        {
            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);

            tagTxtContent.setText(tagContent);

        }
        else
        {
            Toast.makeText(this, "No NDEF records found", Toast.LENGTH_LONG).show();
        }

    }*/

    @Override

    protected void onResume()
    {
        super.onResume();

        if (nfcAdapter != null)
        {
            if (!nfcAdapter.isEnabled())
            {

                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,(ViewGroup) findViewById(R.id.nfc_settings_layout));
                new AlertDialog.Builder(this).setView(dialoglayout)
                        .setPositiveButton("Go to NFC settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {

                            public void onCancel(DialogInterface dialog) {
                                finish(); // exit application if user cancels
                            }
                        }).create().show();

            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
        }
        else
        {
            Toast.makeText(this, "Sorry, this device is not NFC enabled", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage)
    {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);

        if (ndefFormatable == null)
        {
            Toast.makeText(this, "Tag is not NDEF formatable", Toast.LENGTH_LONG).show();
            return;
        }

        try
        {
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(this, "Tag has be written successfully!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Log.e("formatTag: ", e.getMessage());
        }

    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage)
    {
        try
        {
            if (tag == null)
            {
                Toast.makeText(this, "tag object can not be null", Toast.LENGTH_LONG).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null)
            {
                formatTag(tag, ndefMessage);
            }
            else
            {
                ndef.connect();

                if (!ndef.isWritable())
                {
                    Toast.makeText(this, "Tag is write protected", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                Toast.makeText(this, "Tag has be written successfully!", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            Log.e("writeNdefmessage: ", e.getMessage());
        }

    }


   /* private NdefMessage getTagAsNdef() {
        boolean addAAR = true;
        String uniqueId = Common.CARD_ID;
        Log.e("DBG", uniqueId);
        byte[] uriField = uniqueId.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];              //add 1 for the URI Prefix
        payload[0] = 0x01;                                       //prefixes http://www. to the URI

        System.arraycopy(uriField, 0, payload, 1, uriField.length);  //appends URI to payload
        NdefRecord rtdUriRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),
                new byte[0], uniqueId.getBytes());
        if(addAAR) {
            // note:  returns AAR for different app (nfcreadtag)
            return new NdefMessage(new NdefRecord[] {
                    rtdUriRecord, NdefRecord.createApplicationRecord(context.getPackageName())
            });
        } else {
            return new NdefMessage(new NdefRecord[] {
                    rtdUriRecord});
        }
    }
*/


    private NdefRecord createTextRecord (String content)
    {
        try
        {
            byte language [];
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte [] text = content.getBytes("UTF-8");

            final int languageSize = language.length;
            final  int textLength = text.length;

            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte)(languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);



            return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, new String("application/" + context.getPackageName())
                    .getBytes(Charset.forName("US-ASCII")), new byte[0], payload.toByteArray());

        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("createTextRecord: ", e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content)
    {
        NdefRecord ndefRecord = createTextRecord(content);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord,
                NdefRecord.createApplicationRecord(context.getPackageName())});
        return ndefMessage;
    }

   /* public void tglReadWriteOnClick (View view)
    {
        tagTxtContent.setText("");
    }*/

   /* public String getTextFromNdefRecord (NdefRecord ndefRecord)
    {
        String tagContent = null;

        try
        {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding;
            if ((payload[0] & 128) == 0) textEncoding = "UTF-8";
            else textEncoding = "UTF-16";
            int languageSize = payload[0] & 0063;

            tagContent = new String(payload, languageSize+1,payload.length-languageSize-1, textEncoding);

        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("getTextFromNdefRecord: ", e.getMessage());
        }
        return tagContent;
    }*/
}

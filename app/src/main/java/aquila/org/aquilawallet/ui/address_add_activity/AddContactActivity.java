package aquila.org.aquilawallet.ui.address_add_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.aquilaj.uri.AquilaURI;

import aquila.org.aquilawallet.R;
import aquila.org.aquilawallet.ui.newqrscanner.BarcodeCaptureActivity;
import global.AddressLabel;
import global.exceptions.ContactAlreadyExistException;
import aquila.org.aquilawallet.ui.base.BaseActivity;
import aquila.org.aquilawallet.utils.scanner.ScanActivity;

import static android.Manifest.permission_group.CAMERA;
import static aquila.org.aquilawallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 6/8/17.
 */

public class AddContactActivity extends BaseActivity implements View.OnClickListener {

    public static final String ADDRESS_TO_ADD = "address";
    private static final int SCANNER_RESULT = 122;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private View root;
    private EditText edit_name;
    private EditText edit_address;
    private ImageView imgQr;
    private String address;
    private String name;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_new_address, container);
        setTitle("New Address Label");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        edit_name = (EditText) root.findViewById(R.id.edit_name);
        edit_address = (EditText) root.findViewById(R.id.edit_address);
        imgQr = (ImageView) root.findViewById(R.id.img_qr);
        imgQr.setOnClickListener(this);
        edit_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString();
                if(aquilaModule.chechAddress(temp)){
                    address = temp;
                    edit_address.setTextColor(Color.parseColor("#55476c"));;
                }else {
                    edit_address.setTextColor(Color.parseColor("#4d4d4d"));;
                }
            }
        });
        Intent intent = getIntent();
        if (intent!=null){
            if (intent.hasExtra(ADDRESS_TO_ADD)){
                edit_address.setText(intent.getStringExtra(ADDRESS_TO_ADD));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                name = edit_name.getText().toString();
                if (address!=null) {
                    if (name.length() > 0 && address.length() > 0) {
                        try {
                            if (!aquilaModule.chechAddress(address)) {
                                Toast.makeText(this, R.string.invalid_input_address, Toast.LENGTH_LONG).show();
                                return true;
                            }
                            AddressLabel addressLabel = new AddressLabel(name);
                            addressLabel.addAddress(address);
                            aquilaModule.saveContact(addressLabel);
                            Toast.makeText(this, "AddressLabel saved", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        } catch (ContactAlreadyExistException e) {
                            Toast.makeText(this, R.string.contact_already_exist, Toast.LENGTH_LONG).show();
                        }
                    }
                }else {
                    Toast.makeText(this,R.string.invalid_input_address,Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onNavigationBackPressed() {
        // save contact

    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.img_qr){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE){
            if (resultCode== CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    try {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        String address = barcode.displayValue;
                        String usedAddress;
                        String bitcoinUrl = address;
                        String addresss = bitcoinUrl.replaceAll("aquila:(.*)\\?.*", "$1");
                        String label = bitcoinUrl.replaceAll(".*label=(.*)&.*", "$1");
                        String amounta = bitcoinUrl.replaceAll(".*amount=(.*)(&.*)?", "$1");

                        if (aquilaModule.chechAddress(addresss)){
                            usedAddress = addresss;
                        }else {
                            AquilaURI aquilaUri = new AquilaURI(address);
                            usedAddress = addresss;
                        }
                        final String tempPubKey = usedAddress;
                        edit_address.setText(tempPubKey);
                    }catch (Exception e){

                        Toast.makeText(this,"Bad address",Toast.LENGTH_LONG).show();
                    }}
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}

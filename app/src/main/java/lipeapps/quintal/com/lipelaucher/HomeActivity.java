package lipeapps.quintal.com.lipelaucher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity implements GestureOverlayView.OnGesturePerformedListener {
    private PackageManager _manager;
    private List<AppDetails> _apps;
    private ListView _list;

    private AppsArrayAdapter _adapter;

    private EditText _search_label;
    private Intent i;
    Typeface _tf;

    private FloatingActionButton _call;
    private FloatingActionButton _message;
    private FloatingActionButton _camera;

    private GestureLibrary _gestureLib;
    private CoordinatorLayout _coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View inflate = getLayoutInflater().inflate(R.layout.activity_home,null);     // object used to inflate our interface XML
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);                           // overlay used to detect the gestures
        gestureOverlayView.setGestureColor(Color.parseColor("#00000000"));                                // color used to "print" the gesture
        gestureOverlayView.setGestureVisible(true);                                                     // set gesture to visible
        gestureOverlayView.setUncertainGestureColor(Color.parseColor("#00000000"));                       // color when the gesture is not recognized
        //gestureOverlayView.set(Color.parseColor("#00000000"));
        gestureOverlayView.addView(inflate);                                                            // add our inflate view to the gestureOverlay
        setContentView(gestureOverlayView);                                                             // sets the content of the activity
        gestureOverlayView.addOnGesturePerformedListener(this);                                         // adds gesture

        _gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);                           // creates the gesture library from the resource file

        if(!_gestureLib.load()){
            Log.e("gestures", "problems loading gestures file ");
        }

        TextView apps_label = (TextView)findViewById(R.id.apps_label);
        _search_label       = (EditText)findViewById(R.id.search_label);
        _coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        _list = (ListView) findViewById(R.id.app_list);
        _manager = getPackageManager();
        _apps = new ArrayList<AppDetails>();
        _tf = Typeface.createFromAsset(getAssets(), "fonts/edunline.ttf");
        apps_label.setTypeface(_tf);

        _apps =  loadApps();
        addClickListener();

        _tf = Typeface.createFromAsset(getAssets(), "fonts/DisposableDroidBB.ttf");

        _search_label.setTypeface(_tf);
        _search_label.setSelection(4);

        _adapter = new AppsArrayAdapter(this,_apps,R.layout.list_item,getLayoutInflater());
        _list.setAdapter(_adapter);

        _call = (FloatingActionButton) findViewById(R.id.fab);
        _call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(Intent.ACTION_MAIN, null);
                i = _manager.getLaunchIntentForPackage("com.android.contacts");
                HomeActivity.this.startActivity(i);
            }
        });

        _message = (FloatingActionButton) findViewById(R.id.fab3);
        _message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(Intent.ACTION_MAIN,null);
                i = _manager.getLaunchIntentForPackage("com.android.mms");
                HomeActivity.this.startActivity(i);
            }
        });

        _camera = (FloatingActionButton) findViewById(R.id.fab2);
        _camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  i = new Intent(Intent.ACTION_MAIN,null);
                  i = _manager.getLaunchIntentForPackage("com.lge.camera");
                  HomeActivity.this.startActivity(i);
            }
        });
    }

    private ArrayList<AppDetails> loadApps() {

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> phone_activities = _manager.queryIntentActivities(i, 0);
        ArrayList<AppDetails> result = new ArrayList<>();

        for (ResolveInfo ri : phone_activities) {
            AppDetails app = new AppDetails( ri.loadLabel(_manager),  ri.activityInfo.packageName);
            result.add(app);
        }
        System.gc();
        return result;
    }

    public void handleClearClick(View v){
        _search_label.setText("  $ ");
        _search_label.setSelection(4);
        hideKeyboard();
    }

    private void addClickListener(){

        _list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                AppDetails temp = _apps.get(pos);
                _apps.remove(pos);
                _apps.add(0, temp);
                _adapter.removeItem(temp.label.toString());//.add(temp);
                _adapter.notifyDataSetChanged();

                //  Log.i("ClickListener", "selectedApp-> " + temp.label);

                _search_label.setText("  $ ");
                _search_label.setSelection(4);
                hideKeyboard();

                i = _manager.getLaunchIntentForPackage(temp.name.toString());
                HomeActivity.this.startActivity(i);
            }
        });

        _search_label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();

                if (!str.contains("  $ ")) {
                    _search_label.setText("  $ ");
                    _search_label.setSelection(4);
                    str = "  $ ";
                }
                str = str.replace("  $ ", "");
                _adapter.getFilter().filter(str);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

        ArrayList<Prediction> preds = _gestureLib.recognize(gesture);  // asks the library to recgnize the detected gesture
        if(preds.size()>0){                                            // if there is something detected
            Prediction pred = preds.get(0);                            // gets the first element from the array
            if(pred.score>1){

                i = new Intent(Intent.ACTION_MAIN, null);

                String s = pred.name;
                if(s.equals("9gag")) {
                   //Log.i("gesture","9gag"); //com.ninegag.android.app
                    showName("9gag");
                    i = _manager.getLaunchIntentForPackage("com.ninegag.android.app");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("camera")) {
                    showName("camera");
                    i = _manager.getLaunchIntentForPackage("com.lge.camera");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("gallery")) {
                    showName("gallery");
                    i = _manager.getLaunchIntentForPackage("com.android.gallery3d");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("message")) {
                    showName("message");
                    i = _manager.getLaunchIntentForPackage("com.android.mms");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("settings")) {
                    showName("settings");
                    i = _manager.getLaunchIntentForPackage("com.android.settings");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("telefone")) {
                    showName("phone");
                    i = _manager.getLaunchIntentForPackage("com.android.contacts");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("whatsapp")) {
                    showName("whatsapp");
                    i = _manager.getLaunchIntentForPackage("com.whatsapp");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("internet")) {
                    showName("browser");
                    i = _manager.getLaunchIntentForPackage("com.android.browser");
                    HomeActivity.this.startActivity(i);
                }else if(s.equals("lanterna")) {
                    showName("lanterna");
                    i = _manager.getLaunchIntentForPackage("com.devuni.flashlight");
                    HomeActivity.this.startActivity(i);
                }

            }
        }



    }

    private  void showName(String name){
        Snackbar snackbar = Snackbar
                .make(_coordinatorLayout, name, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private class AppsArrayAdapter extends ArrayAdapter {


        private final Context _context;
        private final int _resource;
        private final LayoutInflater _inflater;
        private final List<AppDetails> _items;
        private List<AppDetails> _filtered;
        private AppFilter _myFilter;


        public AppsArrayAdapter(Context context, List<AppDetails> content, int resource,  LayoutInflater inflater) {
            super(context, resource, content);

            this._context  = context;
            this._resource = resource;
            this._inflater = inflater;

            this._items    = new ArrayList<>();
            this._items.addAll(content);
            this._filtered = new ArrayList<>();
            _filtered.addAll(content);
        }

        public void removeItem(String label){
            notifyDataSetInvalidated();

            AppDetails temp=null;
            for(int i =0; i<_items.size();i++) {
                if (this._items.get(i).label.equals(label)) {
                    temp = _items.get(i);
                    _items.remove(i);
                }
            }

            if(temp!=null)
                _items.add(0,temp);

            this._filtered = new ArrayList<>();
            _filtered.addAll(_items);

            notifyDataSetInvalidated();

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            if(convertView == null){
                convertView = _inflater.inflate(_resource, null);
            }
            //    ImageView appIcon = (ImageView)convertView.findViewById(R.id.item_app_icon);
            //    appIcon.setImageDrawable(_items.get(position).icon);

            TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
            appLabel.setText("   > " + _filtered.get(position).label);
            appLabel.setTypeface(_tf);
//            TextView appName = (TextView)convertView.findViewById(R.id.item_app_name);
//            appName.setText(_items.get(position).name);

  //          Log.i("Apps Act", "aqui desenhando apps");

            return convertView;
        }

        @Override
        public Filter getFilter() {

            if(_myFilter==null)
                _myFilter = new AppFilter();

            return _myFilter;

        }
        private  class AppFilter extends Filter {


            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                constraint = constraint.toString().toLowerCase();
                FilterResults result = new FilterResults();


                if(constraint != null && constraint.toString().length() > 0)
                {
                    ArrayList<AppDetails> selectedApps = new ArrayList<>();

//                    Log.i("Filter", "----------------");
//                    Log.i("Filter", "total apps -> "+_items.size());
//                    Log.i("Filter", "constrain -> "+constraint);
//                    Log.i("Filter", "----------------");

                    for(int i =0 ;i<_items.size();i++){

                        AppDetails item = _items.get(i);

//                        Log.i("Filter", "-------**-------");
//                        Log.i("Filter", "item name -> "+item.label);

                        if(item.label.toString().toLowerCase().contains(constraint))
                            selectedApps.add(item);

//                        Log.i("Filter", "selected size -> "+selectedApps.size());
//                        Log.i("Filter", "-------**-------");

                    }

                    result.count = selectedApps.size();
                    result.values = selectedApps;
                }else{
                    synchronized (this) {
                        result.count = _items.size();
                        result.values = _items;
                    }
                }

                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                _filtered = (List<AppDetails>) results.values;
                notifyDataSetChanged();
                clear();

                for(int i = 0, l = _filtered.size(); i < l; i++) {
                    add(_filtered.get(i));
                 //   Log.i("Filter", "selected items -> "+_filtered.get(i).label);
                }
//                Log.i("Filter", "items size ->"+_items.size() );
//                Log.i("Filter", "-------##-------");
                notifyDataSetInvalidated();
            }
        }
    }

}

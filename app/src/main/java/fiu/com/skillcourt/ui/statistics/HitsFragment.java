package fiu.com.skillcourt.ui.statistics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.ui.base.BaseFragment;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


/**
 * Created by April Perry
 */

public class HitsFragment extends BaseFragment {

    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthListener;

    ArrayList<Long> datesList = new ArrayList<Long>();
    ArrayList<Long> greenList = new ArrayList<Long>();
    ArrayList<Long> totalList = new ArrayList<Long>();

    public static HitsFragment newInstance() {
        return new HitsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hits, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView toChange = (TextView) view.findViewById(R.id.hitsTitle);
        toChange.setText("Game Stats: Green Hits vs. Red Hits!");

        datesList.clear();
        greenList.clear();
        totalList.clear();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference("users/" + user.getUid()).child("games");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Map<String, String> map = (Map<String, String>) postSnapshot.getValue();

                    Object date = map.get((Object) "date");
                    datesList.add((Long) date);

                    Object red = map.get((Object) "greenHits");
                    greenList.add((Long) red);

                    Object total = map.get((Object) "totalHits");
                    totalList.add((Long) total);
                }
              createBarGraph(totalList, greenList, datesList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    protected void createBarGraph(ArrayList<Long> totalList, ArrayList<Long> greenList, ArrayList<Long> datesList) {

        if ( (greenList.size() == 0) || (totalList.size() == 0) || (datesList.size() ==0) ) {
            return;
        }

        BarChart bchart = (BarChart) getView().findViewById(R.id.bChart);
        bchart.setBackgroundColor(Color.TRANSPARENT);
        bchart.setTouchEnabled(true);

        // Entry = x axis, y axis
        ArrayList<BarEntry> vals = new ArrayList();
        ArrayList<BarEntry> vals2 = new ArrayList();
        for (int i = 0; i < totalList.size(); i++) {
            float f1 = (float) i;
            vals.add(new BarEntry(f1, Float.parseFloat(totalList.get(i).toString()) - Float.parseFloat(greenList.get(i).toString())));
            vals2.add(new BarEntry(f1, Float.parseFloat(greenList.get(i).toString())));
        }

        BarDataSet dataSet1 = new BarDataSet(vals, "Green Hits");
        dataSet1.setColor(Color.rgb(0, 155, 0));
        BarDataSet dataSet2 = new BarDataSet(vals2, "Red Hits");
        dataSet2.setColor(Color.rgb(155, 0, 0));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String[] values = new String[datesList.size()];
        for (int j = 0; j < datesList.size(); j++) {
            Date date = new Date(datesList.get(j));
            values[j] = simpleDateFormat.format(date).toString().substring(5,7) +"/" + simpleDateFormat.format(date).toString().substring(8,10);
        }

        bchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        bchart.getXAxis().setAxisMinimum(0);

        BarData bData = new BarData(dataSet2, dataSet1);
        bData.setBarWidth(.4f);

        bchart.getAxisLeft().setDrawGridLines(false);
        bchart.getAxisRight().setDrawGridLines(false);

        XAxis newX = bchart.getXAxis();
        newX.setValueFormatter(new MyXAxisValueFormatter(values));

        bchart.setData(bData);
        bchart.groupBars(0, .07f, .01f);
        bchart.invalidate();
    }



    public class MyXAxisValueFormatter implements IAxisValueFormatter, IValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int) value];
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return null;
        }
    }
}

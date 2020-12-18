package com.balychev.tripleimpact;

import android.app.Activity;
import android.os.Bundle;

import com.balychev.tripleimpact.adapter.ProgressAdapter;
import com.balychev.tripleimpact.domain.CurrentState;
import com.balychev.tripleimpact.domain.TrainingHelper;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


public class ProgressActivity extends Activity {

    ExpandableStickyListHeadersListView workoutList;
    //Spinner levelSpinner;
    StickyListHeadersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        //levelSpinner = (Spinner)findViewById(R.id.p_level_selector);
        workoutList = (ExpandableStickyListHeadersListView )findViewById(R.id.workouts_list);

        CurrentState currentState = TrainingHelper.getCurrentState(this);

        adapter = new ProgressAdapter(this, currentState.historyRecords);
        workoutList.setAdapter(adapter);
        /*workoutList.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                if(workoutList.isHeaderCollapsed(headerId)){
                    workoutList.expand(headerId);
                }else {
                    workoutList.collapse(headerId);
                }
            }
        });*/

        /*levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter = new ProgressAdapter(ProgressActivity.this, (position + 1));
                workoutList.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }
}

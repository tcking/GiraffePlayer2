package tcking.github.com.giraffeplayer2.trackselector;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListView;

import com.github.tcking.giraffeplayer2.R;
import com.github.tcking.viewquery.ViewQuery;

/**
 * Created by TangChao on 2017/10/11.
 */

public class TrackSelectorFragment extends DialogFragment{
    private ViewQuery $;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.giraffe_track_selector, container, false);
        $ = new ViewQuery(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ExpandableListView list = $.id(R.id.app_video_track_list).view();

        $.id(R.id.app_video_track_close).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
//                getFragmentManager().beginTransaction().remove(TrackSelectorFragment.this).commit();
            }
        });

        final TracksAdapter tracksAdapter = new TracksAdapter();
        list.setGroupIndicator(null);
        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        list.setAdapter(tracksAdapter);
        tracksAdapter.load(getArguments().getString("fingerprint"));
        int count = tracksAdapter.getGroupCount();
        for ( int i = 0; i < count; i++ ) {
            list.expandGroup(i);
        }
    }
}

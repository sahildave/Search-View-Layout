package xyz.sahildave.widget.sample;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchStaticListFragment extends Fragment {

    public SearchStaticListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_static_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.search_static_list);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(i+" -- \n"+getString(R.string.lorem_1));
        }
        ListViewAdapter adapter = new ListViewAdapter(getActivity(), strings);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        return rootView;
    }

    public class ListViewAdapter extends ArrayAdapter<String> {

        private final Context context;
        private final List<String> values;

        public ListViewAdapter(Context context, List<String> objects) {
            super(context, R.layout.search_static_list_item, objects);
            this.context = context;
            this.values = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.search_static_list_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.card_details);
            textView.setText(values.get(position));
            return rowView;
        }
    }

}

package com.measure.siddhantkushwaha.measure.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.measure.siddhantkushwaha.measure.api.PlacesAPI;
import com.measure.siddhantkushwaha.measure.pojo.SearchedPlace;

import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<SearchedPlace> implements Filterable {

    ArrayList<SearchedPlace> resultList;

    Context mContext;
    int mResource;

    PlacesAPI mPlaceAPI;

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
        mPlaceAPI = new PlacesAPI(mContext);
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public SearchedPlace getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

                    resultList = mPlaceAPI.autocomplete(constraint.toString());
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }
}
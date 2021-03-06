package com.tneciv.blueprint.module.trend;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

import com.tneciv.blueprint.base.BaseListFragment;
import com.tneciv.blueprint.common.Constants;
import com.tneciv.blueprint.common.utils.CheckUtils;
import com.tneciv.blueprint.entity.ShotEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tneciv.blueprint.common.Constants.PAGE;
import static com.tneciv.blueprint.common.Constants.PAGE_SIZE;
import static com.tneciv.blueprint.common.Constants.PER_PAGE;


/**
 * Created by Tneciv
 * on 2016-08-14 16:00 .
 * A fragment to show shots list .
 */
public abstract class TrendFragment extends BaseListFragment implements TrendContract.View, TrendRecyclerAdapter.PaginationListener {

    private TrendContract.Presenter mPresenter;
    private List<ShotEntity> list;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
        recyclerAdapter.removePaginationListener();
    }

    private TrendRecyclerAdapter recyclerAdapter;

    public TrendFragment() {
    }

    @Override
    protected void initRecyclerView() {
        list = new ArrayList<>();
        recyclerAdapter = new TrendRecyclerAdapter(setViewType(), list);
        recyclerAdapter.addPaginationListener(this);
        mRecyclerView.setAdapter(recyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(setLayoutManager(getActivity()));
    }

    protected abstract Fragment setViewType();

    @Override
    public void onRefresh() {
        mPresenter.getShotList(initOptionMap());
    }

    @Override
    public void setPresenter(TrendContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showList(ShotEntity[] shotEntities) {
        if (currentPage == 1) {
            list.clear();
            totalRecord = 0;
        }
        totalRecord += shotEntities.length;
        list.addAll(Arrays.asList(shotEntities));
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading() {
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showError(Throwable throwable) {
        String msg = CheckUtils.checkString(throwable.getMessage());
        mEmptyBtn.setText(msg);
    }

    @Override
    public void onChange(int position) {
        currentPage = position / PAGE_SIZE + 1;
        mPresenter.loadMore(currentPage, totalRecord, setOptionType());
    }

    /**
     * set layoutManager of RecyclerView .
     *
     * @param context
     * @return LayoutManager
     */
    public abstract RecyclerView.LayoutManager setLayoutManager(Context context);

    /**
     * set the optionMap to get shots .
     *
     * @return optionMap .
     */
    public abstract Map<String, String> initOptionMap();

    public Map<String, String> defaultOptionMap() {
        Map<String, String> map = setOptionType();
        String sortType = map.get(Constants.SHOTS_NAME);
        String trendType = map.get(Constants.SHOTS_TYPE);
        map.put(sortType, trendType);
        map.put(PAGE, "1");
        map.put(PER_PAGE, String.valueOf(PAGE_SIZE));
        map.remove(Constants.SHOTS_NAME);
        map.remove(Constants.SHOTS_TYPE);
        return map;
    }

    public abstract Map<String, String> setOptionType();

    /**
     * set shotname & shotType for option map .
     *
     * @param shotName
     * @param shotType
     * @return
     */
    public Map<String, String> setOptionType(String shotName, String shotType) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.SHOTS_NAME, shotName);
        map.put(Constants.SHOTS_TYPE, shotType);
        return map;
    }

    protected long exitTime = 0;

    protected void doubleBack2Exit() {
        if ((System.currentTimeMillis() - exitTime) > 3000) {
            mRecyclerView.smoothScrollToPosition(0);
            exitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
    }

}

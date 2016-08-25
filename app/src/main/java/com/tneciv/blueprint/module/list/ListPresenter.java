package com.tneciv.blueprint.module.list;

import com.tneciv.blueprint.base.BasePresenterImpl;
import com.tneciv.blueprint.entity.ShotEntity;
import com.tneciv.blueprint.retrofit.ApiServiceFactory;
import com.tneciv.blueprint.retrofit.ShotService;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

import static com.tneciv.blueprint.common.Constants.PAGE;
import static com.tneciv.blueprint.common.Constants.PAGE_SIZE;
import static com.tneciv.blueprint.common.Constants.PER_PAGE;
import static com.tneciv.blueprint.common.Constants.SORT;
import static com.tneciv.blueprint.common.Constants.SORT_TYPE_VIEWS;

/**
 * Created by Tneciv
 * on 2016-08-17 18:20 .
 */
public class ListPresenter extends BasePresenterImpl implements ListContract.Presenter {

    private ListContract.View mView;

    public ListPresenter(ListContract.View view) {
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        HashMap<String, String> options = new HashMap<>();
        options.put(SORT, SORT_TYPE_VIEWS);
        options.put(PAGE, "1");
        options.put(PER_PAGE, String.valueOf(PAGE_SIZE));
        getShotList(options);
    }

    @Override
    public void unsubscribe() {
        super.onUnsubscribe();
    }

    @Override
    public void loadMore(int currentPage, int pageSize, int totalRecord, String sortType) {
        if (currentPage > totalRecord / pageSize) {
            Map<String, String> options = new HashMap<>();
            options.put(SORT, sortType);
            options.put(PAGE, String.valueOf(currentPage));
            options.put(PER_PAGE, String.valueOf(pageSize));
            getShotList(options);
        }
    }

    @Override
    public void getShotList(Map<String, String> options) {
        mView.showLoading();
        ShotService shotService = ApiServiceFactory.getInstance().create(ShotService.class);
        Observable<ShotEntity[]> shotList = shotService.getShotListWithQueryMap(options);
        super.addSubscription(shotList, new Subscriber<ShotEntity[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mView.hideLoading();
                mView.showError();
            }

            @Override
            public void onNext(ShotEntity[] shotEntities) {
                mView.hideLoading();
                mView.showList(shotEntities);
            }
        });
    }

}
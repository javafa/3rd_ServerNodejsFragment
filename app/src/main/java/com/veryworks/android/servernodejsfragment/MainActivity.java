package com.veryworks.android.servernodejsfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class MainActivity extends AppCompatActivity {
    private List<Bbs> data = new ArrayList<>();
    ListFragment listFragment;
    DetailFragment detailFragment;

    private static final int FROM_CREATE = 1;
    private static final int FROM_DETAIL = 2;

    public List<Bbs> getData(){
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loader(FROM_CREATE);

    }

    private void setFragment(){
        listFragment = new ListFragment();
        detailFragment = new DetailFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, listFragment)
                .commit();
    }

    private void addFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack("detailFragment")
                .commit();
    }

    private void popFragment(){
        onBackPressed();
    }

    private void loader(int from) {
        // 1. 레트로핏 생성
        Retrofit client = new Retrofit.Builder()
                .baseUrl(IBbs.SERVER)
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 2. 서비스 연결
        IBbs myServer = client.create(IBbs.class);

        // 3. 서비스의 특정 함수 호출 -> Observable 생성
        Observable<ResponseBody> observable = myServer.read();

        // 4. subscribe 등록
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                responseBody -> {
                    // 1. 데이터를 꺼내고
                    String jsonString = responseBody.string();
                    Gson gson = new Gson();
                    Bbs data[] = gson.fromJson(jsonString, Bbs[].class);
                    // 2. 데이터를 아답터에 세팅하고
                    for(Bbs bbs : data){
                        MainActivity.this.data.add(bbs);
                    }
                    // 3. 아답터 갱신
                    // 호출된 곳에 따라 처리가 달라진다.
                    switch (from){
                        case FROM_CREATE:
                            setFragment();
                            break;
                        case FROM_DETAIL:
                            listFragment.refresh();
                            break;
                    }

                }
            );
    }

    public void goDetail() {
        addFragment(detailFragment);
    }

    public void goList() {
        popFragment();
        data.clear();
        loader(FROM_DETAIL);
    }
}

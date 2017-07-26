package com.veryworks.android.servernodejsfragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    private MainActivity mainActivity;

    private EditText editTitle;
    private EditText editAuthor;
    private EditText editContent;
    private Button btnPost;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        editTitle = (EditText) view.findViewById(R.id.editTitle);
        editAuthor = (EditText) view.findViewById(R.id.editAuthor);
        editContent = (EditText) view.findViewById(R.id.editContent);
        btnPost = (Button) view.findViewById(R.id.btnPost);
        btnPost.setOnClickListener(v->{
            String title = editTitle.getText().toString();
            String author = editAuthor.getText().toString();
            String content = editContent.getText().toString();

            postData(title, author, content);
        });
    }

    private void postData(String title, String author, String content){
        // 0. 입력할 객체 생성
        Bbs bbs = new Bbs();
        bbs.title = title;
        bbs.author = author;
        bbs.content = content;

        // 1. 레트로핏 생성
        Retrofit client = new Retrofit.Builder()
                .baseUrl(IBbs.SERVER)
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 2. 서비스 연결
        IBbs myServer = client.create(IBbs.class);

        // 3. 서비스의 특정 함수 호출 -> Observable 생성
        Gson gson = new Gson();
        // bbs 객체를 수동으로 전송하기 위해서는
        // bbs 객체 -> json String 변환
        // RequestBody 에 미디어타입과, String 으로 벼환된 데이터를 담아서 전송
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                gson.toJson(bbs)
        );

        Observable<ResponseBody> observable = myServer.write(body);

        // 4. subscribe 등록
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        responseBody -> {
                            //String result = responseBody.string(); // 결과코드를 넘겨서 처리...
                            resetInput();
                            mainActivity.goList();
                        }
                );
    }

    private void resetInput() {
        editTitle.setText("");
        editAuthor.setText("");
        editContent.setText("");
    }
}

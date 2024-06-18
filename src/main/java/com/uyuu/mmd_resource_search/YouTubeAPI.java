package com.uyuu.mmd_resource_search;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

///////////////////////////////////
//////  youtube関連機能クラス  ////////
///////////////////////////////////

public class YouTubeAPI {
	private String fileName; //APIkey
    private long limit; // 最大取得数
    private YouTube youtube;
    private String apiKey;
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    // コンストラクタ
    public YouTubeAPI(String fileName, long limit) {
    	this.fileName = fileName;
    	this.limit = limit;
    	this.youtube = getYouTube("myapp");
    	setAPIkey();
    }
    // get
    public String getApiKey() {
    	return apiKey;
    }
    //////////////////////////
    //// youtubeインスタンス作成
    //////////////////////////
    private YouTube getYouTube(String appName) {
    	youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(appName).build();
    	return youtube;
    }
    
    //////////////////////////
    /////// APIキーをセット
    //////////////////////////
    private void setAPIkey() {
    	// APIキーのファイル読み取り
        Properties properties = new Properties();
        try {
            InputStream in = YouTubeAPI.class.getResourceAsStream("./" + fileName);
            properties.load(in);
        } catch (IOException e) {
            System.err.println("There was an error reading " + fileName + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
        // API変数を取ってくる
        this.apiKey = properties.getProperty("youtube.apikey");
    }
    
    ///////////////////////
    /////// ワード検索
    ///////////////////////
    public void searchVideo(String q, String in_title) {
    	try {
            //検索リクエストのインスタンス
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(apiKey);  // APIキー
            search.setQ(q); // 検索ワード
            search.setType("video");// 動画のみ
            search.setOrder("relevance"); // 関連度順           
            search.setFields("items(id/kind, id/videoId, snippet/title, snippet/description, snippet/channelTitle)");
            search.setMaxResults(limit); // 最大取得数

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
            	//結果を表示
            	searchPrint(searchResultList.iterator(), q, in_title);
            }
        // エラー処理
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    ///////////////////////////////
    /////// ワード検索出力テンプレ
    ///////////////////////////////
    public void searchPrint(Iterator<SearchResult> iteratorSearchResults, String query, String in_title) {
        if (!iteratorSearchResults.hasNext()) {
            System.out.println("youtubeに該当する動画はありませんでした。");
        }
        while (iteratorSearchResults.hasNext()) {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();
            if (rId.getKind().equals("youtube#video")) {
            	// 元動画を除く
            	if(singleVideo.getSnippet().getTitle().equals(in_title)) continue;             
            	String description = singleVideo.getSnippet().getDescription();
                System.out.println(" URL: "+"https://www.youtube.com/watch?v="+rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Description: " + description);
                // 素材配布情報を探す
                Tool.pickSozai(description);
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }

    //////////////////////////
    /////// URL > ID抽出
    //////////////////////////
    public String getVideoID(String url) {
    	String id = null;
    	if(url!=null && url.contains("youtube.com/watch?v=")) {
    		String[] sp = url.split("v=");
    		if(sp.length>1) id = sp[1].substring(0,11);
    	}else if(url!=null && url.contains("https://youtu.be/")) {
    		String[] sp = url.split("youtu.be/");
    		if(sp.length>1) id = sp[1].substring(0,11);
    	}else {
    		System.out.println("URLがyoutubeの形式と異なっている可能性があります。");
    	}
    	return id;
    }

    /////////////////////////////////////
    //// ID to [タイトル,概要欄,チャンネル名]
    /////////////////////////////////////
    public String[] getDetail(String videoID) {
    	try {
    		// 動画情報取得インスタンス
    		YouTube.Videos.List videoReq = this.youtube.videos().list("snippet");
    		videoReq.setKey(apiKey);  // APIキー
    		videoReq.setId(videoID);
    		VideoListResponse videoResponse = videoReq.execute();
    		String description = null;
    		String title = null;
    		String channel = null;
    		if (videoResponse != null) {
    			Video video = videoResponse.getItems().get(0);
    			title = video.getSnippet().getTitle(); // タイトル
        		description = video.getSnippet().getDescription(); //概要欄
        		channel = video.getSnippet().getChannelTitle();
    		}else {
    			description = "動画の概要欄を取得できませんでした。";
    		}
    		String[] details = { title,description,channel };
    		return details;
    	}catch (Exception e) {
            e.printStackTrace();
            String[] details = { "動画のタイトルを取得できませんでした。","動画の概要欄を取得できませんでした。" };
            return details;
        }
    }
}

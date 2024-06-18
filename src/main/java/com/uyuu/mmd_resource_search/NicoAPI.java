package com.uyuu.mmd_resource_search;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

///////////////////////////////////
/////////  ニコニコ関連クラス  //////////
///////////////////////////////////

public class NicoAPI {
	private int limit;
	private String tag1;
	private String tag2;
	public NicoAPI(int limit,String tag1, String tag2) {
		this.limit = limit;
		this.tag1 = tag1;
		this.tag2 = tag2;
	}
	///////////////////////////
	///////// ワード検索
	///////////////////////////	
	public void wordSearch(String q, String in_title) throws IOException {
		HttpClient httpClient = HttpClient.newHttpClient();
		// リクエストURL作成
		String reqURL = req_word(q, limit, tag1, tag2);
		// APIリクエストの構築
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(reqURL))
				.header("Content-Type", "application/json")
				.build();
		try {
			// APIリクエストの送信とレスポンスの取得
			HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			// 検索結果表示
			SearchPrint(response.body(),in_title);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	///////////////////////////
	///////// リクエスト作成
	///////////////////////////	
	public String req_word(String q, int limit, String tag1, String tag2) {
		// 日本語をUTF-8エンコード
		try {
			q = URLEncoder.encode(q, "UTF-8");
			tag1 = URLEncoder.encode(tag1, "UTF-8");
			tag2 = URLEncoder.encode(tag2, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 前半テンプレ
		String apiURL = "https://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search?";
		// クエリ部分
		String queryString = String.format(
				"q=%s&targets=title&filters[tags][0]=%s&filters[tags][1]=%s&fields=contentId,title,description,tags,viewCounter&_sort=-viewCounter&_offset=0&_limit=%d",
				q, tag1, tag2, limit);
		return apiURL + queryString;

	}
	
	///////////////////////////////////
	/////////  検索結果出力
	///////////////////////////////////
	public void SearchPrint(InputStream inputStream, String in_title) {
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootArray = mapper.readTree(inputStream);
			JsonNode data = rootArray.get("data");
			JsonNode meta = rootArray.get("meta");
			if (data==null||meta.get("totalCount").asInt()==0) {
				System.out.println("ニコニコに該当する動画はありませんでした。");
			}else {
				// 出力
				for (JsonNode node : data) {
					String contentId = node.get("contentId").asText();
					String title = node.get("title").asText();
					String description = node.get("description").asText();
					// 概要欄のタグ削除
					String desc_noTag = description.replaceAll("<.*?>","");
					// 元動画を除く
					if(in_title.equals(title)==false) {
						System.out.println(" URL: "+"http://www.nicovideo.jp/watch/" + contentId);
						System.out.println(" Title: " + title);
						System.out.println(" Description: " + desc_noTag);
						// 素材配布情報がないか探す
		                Tool.pickSozai(description);
						System.out.println("\n-------------------------------------------------------------\n");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////
	///////// URL > ID抽出
	////////////////////////////////
	public String getNicoID(String url) {
		String id = null;
		if (url != null && url.contains("://www.nicovideo.jp/watch/sm")) {
			String[] sp = url.split("sm");
			if (sp.length > 1)
				id = sp[1];
			if (id.length() < 6 || id.length() > 9) {
				System.out.println("正しいIDが取得できませんでした。");
				id = null;
			}
		} else {
			System.out.println("ニコニコのURL形式と合っていません");
		}
		return id;
	}
	
	//////////////////////////////////////
	//// ID > [タイトル,概要欄,チャンネル名]
	//////////////////////////////////////
	public String[] getDetail(String id) {
		HttpClient httpClient = HttpClient.newHttpClient();
		String reqURL = "http://ext.nicovideo.jp/api/getthumbinfo/sm" + id;
		// APIリクエストの構築
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(reqURL))
				.header("Content-Type", "application/json")
				.build();
		String[] details=null;
		try {
			// APIリクエストの送信とレスポンスの取得
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			String xml = response.body(); //xml
        	details = readXML(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return details;
	}

	/////////////////////////////////////
	//// xml > [タイトル,概要欄,チャンネル名]
	/////////////////////////////////////
	private String[] readXML(String xml) {
		Document document = null;
		try (StringReader reader = new StringReader(xml)) {
			InputSource source = new InputSource(reader);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
 	
		String title = null, description = null, channel = null;
		NodeList thumbList = document.getElementsByTagName("thumb");
		Element thumb = (Element) thumbList.item(0);
		// タイトル
		NodeList titleList = thumb.getElementsByTagName("title");
		for (int j = 0; j < titleList.getLength(); j++) {
			Element titleElement = (Element) titleList.item(j);
			title = titleElement.getTextContent();
		}
		// 概要欄
		NodeList descList = thumb.getElementsByTagName("description");
		for (int j = 0; j < descList.getLength(); j++) {
			Element descElement = (Element) descList.item(j);
			description = descElement.getTextContent();
		}
		// チャンネル名
		NodeList channelList = thumb.getElementsByTagName("user_nickname");
		for (int j = 0; j < descList.getLength(); j++) {
			Element channelElement = (Element) channelList.item(j);
			channel = channelElement.getTextContent();
		}

		String[] details = { title,description,channel };
		return details;
	}

}

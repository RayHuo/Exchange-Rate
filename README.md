# Exchange-Rate
Android App for exchange rate

功能：输入数额，选择两种币种，然后给出数额转换成第二种币种的金额。

解决方案：

1. 直接使用java或者java调用python访问："https://www.baidu.com/s?wd=320%20港元%20日元" 或者 "https://www.baidu.com/s?wd=320 港元 日元"，然后从返回的html页面中爬取出需要的汇率结果，然后显示在手机上。

2. java调用python或者java直接网络爬虫。
<br />直接使用java，当前android中运行python比较麻烦，使用kivy等外部库会导致APP的size非常大，当前没有找到较为完美的解决方案，所以直接java爬虫好了。
<br /> 形如：
<pre><code>
String origin = "http://www.baidu.com/s?wd=";
String urlStr = URLEncoder.encode("320 港元 日元" ,"UTF-8");
String address = origin + urlStr;

URL url=new URL(address);
InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
BufferedReader br = new BufferedReader(isr);

String str = null;
while((str=br.readLine()) != null) {
  // ...
}
</code></pre>

3. 先从网页上把当前所有流通币种的名称抓取下来，保存在app的数据库中，这个做一次存好就行了。

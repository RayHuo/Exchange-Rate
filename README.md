# Exchange-Rate

## 功能
输入数额，选择两种币种，然后给出数额转换成第二种币种的金额。

## 解决方案

1. 直接使用java或者java调用python访问："https://www.baidu.com/s?wd=320%20港元%20日元" 或者 "https://www.baidu.com/s?wd=320 港元 日元"，然后从返回的html页面中爬取出需要的汇率结果，然后显示在手机上。

2. java调用python或者java直接网络爬虫。
<br />直接使用java，当前android中运行python比较麻烦，使用kivy等外部库会导致APP的size非常大，当前没有找到较为完美的解决方案，所以直接java爬虫好了。
<br /> 形如：
```java
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
```

3. 先从网页上把当前所有流通币种的名称抓取下来，保存在app的数据库中，这个做一次存好就行了。


## 说明
对于访问网络获取数据：

android 2.3之后都不允许在UI主线程执行较为耗时的操作，可以在onCreate函数的最开始加入`StrictMode`以允许在UI主线程进行网络访问。然而对于较为费时的操作，这样容易造成“假死”现象，即UI卡主了。故访问网络获取数据这样的操作应该另开子线程进行。本例使用Handler和Thread完成。其中：
* Handler默认绑定UI主线程，故重写其handlerMessage方法可以更新UI。
* Thread开一个新的子线程，其中的run方法执行网络访问，并通过调用handler的sendMessage方法，把得到的信息发送到UI主线程的messageQueue中，以待handler操作。
* handlerThread类是一个包含了looper的Thread子类，也可以使用它来完成。


## 未实现功能
* 如果没有输入金额，提醒输入；
* 从网上抓取当前各种流通的币种名称，update到spinner的adapter中；
* 寻找不用整个网页抓取下来（这个会比较慢），只获取需要的金额信息的方法。

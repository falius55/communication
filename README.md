# communication

## Description
通信用のクラス群です。ソケットのノンブロック通信およびBluetooth通信を同じインターフェースで利用することができます。

## Requirement
依存関係はgradleで管理しています。JDKは基本的にjava8が必要ですが、タグ名に&がついているものはjava7でも動作するようにしています(Androidアプリでも使えるように)。

## Installation
build.gradle
```
repositories {
    maven { url 'https://jitpack.io' }
    maven { url 'http://www.pyx4me.com/maven2-snapshot' }  // bluecoveに依存したbluetooth用クラスが含まれるため
}

dependencies {
    compile 'com.github.falius55:communication:1.5.0'
}

```

## Usage
基本的な使用例は下記の通りです。各クラスの詳細に関してはdoc/javadoc内のindex.htmlからjavadocが参照できます。※下記の例ではチェック例外はメソッドのthrows宣言で呼び出し元に投げている前提で省略しています。
### クライアントが送信した文字列をサーバーが大文字にした文字列と文字数を送り返す例
#### サーバー
```
int port = 9001;
/*
 * Swapperは受信データを受け取って送信データを返す。
 * サーバーはそのSwapperを返すSwapperFactoryをコンストラクタに渡す。
 */
try (Server server = new NonBlockingServer(port, new SwapperFactory(){
    @Override
    public Swapper get() {
        // OnceSwapperは送受信をそれぞれ一回だけ行う際に利用する
        return new OnceSwapper() {
            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                String rcv = receiveData.getString();
                SendData sendData = new BasicSendData();
                sendData.put(rcv.toUpperCase());
                sendData.put(rcv.length());
                return sendData;
            }
        }
    }
}); Scanner sc = new Scanner(System.in)) {

    // サーバーは別スレッドで動くためのメソッドしか用意していない
    server.startOnNewThread();

    // "stop"と入力するまでメインスレッドは待機
    // closeメソッドかshutdownメソッドが呼ばれるまでサーバーは起動し続けるので注意
    // breakでループを抜け、そのままtry-with-resources文を抜けることで
    //    closeメソッドが呼ばれてシャットダウンされる
    while(true) {
        String line = sc.nextLine();
        if (line.equals("stop")) {
            break;
        }
    }
}
```
#### クライアント
```
String host = "localhost";
int port = 9001;

// SwapClientは送信から受信までの間をブロックする処理を行う際のインターフェース
// 戻り値から受信データを受け取れる送信メソッドを実装している
SwapClient client = new NonBlockingClient(host, port);
SendData sendData = new BasicSendData();
sendData.put("abcd");

// SwapClient#send(SendData)は送信が一回のみの場合の簡易メソッド
ReceiveData receiveData = client.send(sendData);

// データは送信相手がputした順序で格納されているので、
//   同じ順序でgetして取得する
System.out.println(receiveData.getString());  // -> "ABCD"
System.out.println(receiveData.getInt());  // -> 4
```
SwapClient#sendメソッドは同一スレッドで動くので、下記のようにClient自体をCallableとして扱う方法もあります。
Client#startOnNewThreadメソッドを使用すると下記と同じことができます。
```
String host = "localhost";
int port = 9001;
ExecutorService executer = Executors.newSingleThreadExecutor();
Client client = new NonBlockingClient(host, port, nwe OnceSwapper() {
    @Override
    public SendData swap(String remoteAddress, ReceiveData receiveData) {
        // クライアントは受信より先に送信する必要があるため、
        // まだ一度も送信していない段階では受信データにnullが入っており、
        // ここではreceiveDataは使えないことに注意
        SendData sendData = new BasicSendData();
        sendData.put("abcd");
        return sendData;
    }
});

client.addOnReceiveListener(new OnReceiveListener() {
    @Override
    public void onReceive(String remoteAddress, ReceiveData receiveData) {
        // 受信直後に呼ばれる
        System.out.println(receiveData.getString());  // -> "ABCD"
        System.out.println(receiveData.getInt());  // -> 4
    }
});

Future<ReceiveData> future = executor.submit(client);
// Future<ReceiveData> future = client.startOnNewThread();

// sendメソッド、startメソッドの戻り値に相当する最終受信データがFuture#getメソッドの戻り値で取得できる
// しかし、このケースではOnReceiveListener#onReceiveメソッド内で受信データを消費しているため
// retの中身は空(retはnullではないが、ret.get()とするとnullが返ってくる)
ReceiveData ret = future.get();
```
### RepeatSwapperで送受信の度に値を繰り返しインクリメントしていく例
送受信を何度も繰り返し行うにはRepeatSwapperを使います。
#### サーバー
```
/*
 * 上の例と違うのはSwapperの種類とswapメソッド内での処理(回数管理)
 * クライアント側の接続切断を検知するとサーバー側も切断するので、回数管理は必須ではない。
 */
int repeatLen = 10;
int port = 9001;
try (Server server = new NonBlockingServer(port, new SwapperFactory() {

    @Override
    public Swapper get() {
        return new RepeatSwapper() {
            // Swapperはひとつの接続を通して共有される(異なる接続先には異なるSwapper)ので、
            // フィールドを活用できる
            private int count = 0;  // 回数管理用

            @Override
            public SendData swap(String remoteAddress, ReceiveData receiveData) {
                int rcv = receiveData.getInt();
                SendData sendData = new BasicSendData();
                sendData.put(rcv + 1);

                count++;
                if (count == repeatLen) {
                    // 送受信はrepeatLen回まで
                    // finishメソッドを呼ぶと、この時のデータを送信してから接続を切る
                    // このメソッドを呼ばないと永遠にやりとりしようとするので注意
                    finish();
                }
                return sendData;
            }

        };
    }
}); Scanner sc = new Scanner(System.in)) {
    server.startOnNewThread();

    while(true) {
        String line = sc.nextLine();
        if (line.equals("stop")) {
            break;
        }
    }
}
```
#### クライアント
```
int repeatLen = 10;
String host = "localhost";
int port = 9001;

SwapClient client = new NonBlockingClient(host, port);

ReceiveData receiveData = client.start(new RepeatSwapper() {
    private int count = 0;

    @Override
    public SendData swap(String remoteAddress, ReceiveData receiveData) {
        SendData sendData = new BasicSendData();
        if (count == 0) {
            // クライアントの場合だけ、一回目の受信ではreceiveDataにnullが入っているので注意
            sendData.put(0);
        } else {
            int rcv = receiveData.getInt();
            sendData.put(rcv + 1);
        }

        count++;
        if (count == repeatLen) {
　　　　　　　　　　// finishメソッドを呼ぶと、この時のデータを送信して、最後に受信して終わり
            // finishメソッドを呼んでから接続を切るタイミングがサーバーとは違うので注意
            // nullをここで返すこともできるが、直後に切断されてしまい、最後の送受信が行われない。
            finish();
        }
        return sendData;
    }

});

// 最後に受信したデータはswapメソッドの引数には渡されないので、
// startメソッドの戻り値として渡されるReceiveDataで最終結果を確認する
System.out.println(receiveData.getInt());  // -> 19
```
送受信の回数があらかじめ決まっている場合はFixedRepeatSwapperを使うことで回数管理を省略できます。以下は上記をFixedRepeatSwapperで書き換えた例
```

int repeatLen = 10;
String host = "localhost";
int port = 9001;

SwapClient client = new NonBlockingClient(host, port);

ReceiveData receiveData = client.start(new FixedRepeatSwapper(repeatLen) {

    @Override
    public SendData onSwap(String remoteAddress, ReceiveData receiveData) {
        SendData sendData = new BasicSendData();
        if (receiveData == null) {
            // 一回目の受信はreceiveDataがnull
            sendData.put(0);
        } else {
            int rcv = receiveData.getInt();
            sendData.put(rcv + 1);
        }
        return sendData;
    }

});

System.out.println(receiveData.getInt());  // -> 19
```
### JITClientを使って任意のタイミングで送信
上記のSwapperを使った送信データの作成方法では、送信のタイミングを制御したり送信データの作成を異なるスレッドから行うことはしづらくなります。また、接続を切断するタイミングもSwapperにより判断されるため、制御が面倒です。JITClientを使用することで、接続を維持して必要な時に送信データを作成して送信し、closeメソッドで接続を切断する、といったことが可能になります。
```
String host = "localhost";
int port = 9001;

// 実装クラスはNonBlockingClientではなくNonBlockingJITClient
// OnReceiveListenerは必須
JITClient client = new NonBlockingJITClient(host, port new OnReceiveListener() {
    @Override
    public void onReceive(Strin remote, ReceiveData data) {
        System.out.println("receive: " + data.getString());
    }
});

try {
    for (int i = 0; i < 10; i++) {
        Thread.sleep(i * 10);
        SendData sendData = new BasicSendData();
        sendData.put(i);
        // JITClient#sendメソッドはスレッドセーフ
        client.send();  // 戻り値はなし(void)
    }
} catch (InterruptedException e) {
    e.printStackTrace();
} finally {
    client.close();
}
```
### Bluetooth
#### サーバー
サーバーはServerの実装クラスをBluetoothServer(String uuid, SwapperFactory swapperFactory)にするだけで上記と同じ通信をブルートゥースで行うことができます。
#### クライアント
クライアントは付近から接続できるデバイスを探索する必要があるので、以下に使用例を示します。
```
final String UUID = "97d38833e31a4a718d8e4e44d052ce2b";

// 接続先のデバイスを取得する。
RemoteDevice selectedDevice;
try (DeviceSearcher searcher = new DeviceSearcher()) {
    // 付近からペアリング済のデバイスを探索する。
    // 探索は非同期で行われるので、Futureを受け取る。
    Future<Set<RemoteDevice>> future = searcher.searchPairedDevice();
    System.out.println("search device");
    RemoteDevice[] devices = future.get().toArray(new RemoteDevice[0]);

    // デバイスを番号付きで表示し、標準入力で選択する。
    for (int i = 0; i < devices.length; i++) {
        System.out.printf("%d: %s%n", i, devices[i].getFriendlyName(true));
    }
    try (Scanner sc = new Scanner(System.in)) {
        System.out.print("choose device number: ");
        String line = sc.nextLine();
        int deviceNum = Integer.parseInt(line);
        selectedDevice = devices[deviceNum];
    }

}

// クライアントのインスタンスを作成してしまえば後はNonBlockingClientと同じ
SwapClient client = new BluetoothClient(UUID, selectedDevice);

SendData sendData = new BasicSendData();
sendData.put("abcde");

ReceiveData receiveData = client.send(sendData);

String ret = receiveData.getString();
System.out.println("ret: " + ret);

```
ブルートゥース実装クライアントにはBluetoothJITClientもあり、こちらはBluetoothClientのJITClient実装です。
### 扱えるデータの拡張
ExtendableSendData及びExtendableReceiveDataを継承したクラスを使うことで扱えるデータを増やすことができます。
```
// 送信データ
SendData sendData = new BasicSendData();
FileSendData fileData = new FileSendData(sendData);
File file = new File("sample.txt");
fileData.put(file);



// 受信データ
FileReceiveData fileData = new FileReceiveData(receiveData);
File file = new File("rcv.txt");
fileData.getAndSave(file);  // 送信されてきたsample.txtの内容をrcv.txtという名前で保存
```
扱えるデータ
* ファイル(FileSendData, FileReceiveData)
* List, Map(CollectionSendData, CollectionReceiveData)
* Serializable(ObjectSendData, ObjectReceiveData)
* 配列(ArraySendData, ArrayReceiveData)


講真的，我不喜歡那隻黃色大象，所以請容許我把它碼掉。
(好像碼的還不錯 ...)

![](https://i.imgur.com/RMRVRBF.png)

這是一個練習寫在 Hadoop 中執行 MapReduce 程式的紀錄。

<!-- more -->

# 關於 Hadoop 

這邊不詳細說明與介紹，只提及有關此文章有關之事。

1. Hadoop 是一個大型的檔案儲存系統，分散式 DB。
2. Hadoop 提供 HDFS 的分散式儲存功能，以及叫做 MapReduce 的數據處理方式。
3. 你可以把 MapReduce 的過程想成 multi thread，他開很多分支去做 Map (處理與計算)，再把結果全部統整再一起。
    如果你用過 BT 或是 IDM 這種下載軟體，那應該可以很好理解 (分支處理再統合起來)
4. 舉例 : 我想要數整個圖書館的書，Mapper 負責叫一群人數不同書架，Reducer 負責把大家回報的加起來。|
    實際你寫 mapper 和 reducer 時，也是要以此方針來考量 input 和 output。
    
    
# 關於本次作業

雖然您可能是因為正在做其他 project 或是遇到其他問題而找到這篇，
但是 ... 不太好意思的是，本篇只記錄完成作業之遇到問題與注意事項
像是架設 Hadoop 這部分，我就沒有實作到，因為我們是用 Docker (直接提取別人建好的 image)。

- 要求 : 

1. 用 Docker 去跑 Hadoop。
2. Write a map reduce application using Python to do the log analysis of apache2 web server access log.
3. The output of the application: visits per hour.

![Output 範例](https://i.imgur.com/GPmQvcX.png)


- 使用 Python 完成。
- 因為要在 VPS 裡面搞 Hadoop，如果沒有足夠的 Ram 和空間，會直接炸開。
    所以我使用本地端的 Docker 去完成，也就是 Windows (with wsl)
  - WSL : Windows Subsystem for Linux


# 準備環境過程

這篇是一篇很不錯的例子，在講解 Word Count 使用 MapReduce 去實作。
[Writing An Hadoop MapReduce Program In Python](https://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/)

不過我想大部分人都應該看過上面那篇 XD


再來是一些準備 : 

1. [hadoop-cluster Repo](https://github.com/sdwangntu/hadoop-cluster)
  - 除非你跟我做同一個作業，不然大可不必用這個 repo ... 恩。
  - 作者不是我，這個 Repo 內有什麼 Bug 我也無法修改 ...。
2. [Docker Desktop Version](https://www.docker.com/get-started)。
3. WSL (不確定是不是必須，但根據官方說法，使用 WSL2 引擎效能會提升。)
  - [Docker Desktop WSL 2 backend](https://docs.docker.com/docker-for-windows/wsl/)
4. 你常用的 Terminal (在 Windwos 上)


## 準備環境 : Docker image

 {% note info %} 
如果不是跟我同個作業，這段不用看。
 {% endnote %}

Windows 的 Docker 安裝也沒什麼特別的，就一直按確認就好。
不過，如果沒開啟虛擬化技術，好像會不能跑。

關於這點，請去根據提示開啟 Hyper-V (安裝時其實就會幫你開了)
然後大部分的 CPU 都需要去開起虛擬化技術。
Intel 的就叫 Intel Virtualization Technology，AMD 叫 SVM Mode
例如我的 R7 3700X 就要去 BIOS 開啟 SVM Mode (Enable it)。 

跟這篇文章借個圖，大概長這樣。
[如何在我的電腦上啟用虛擬化技術(VT)?](https://support.bluestacks.com/hc/zh-tw/articles/115003174386-%E5%A6%82%E4%BD%95%E5%9C%A8%E6%88%91%E7%9A%84%E9%9B%BB%E8%85%A6%E4%B8%8A%E5%95%9F%E7%94%A8%E8%99%9B%E6%93%AC%E5%8C%96%E6%8A%80%E8%A1%93-VT-)

![要改成 Enable 不是 Disable 嘿 !](https://i.imgur.com/sUU5vDy.png)


裝完 Docker 之後，你可以在任意目錄開啟你的 Terminal，開始下一些關於 docker image 的命令。


關於這個作業，有個特別的點是 ... 在上面的 Repo 內的 Readme 先寫了 Build 某個 image
但其實，作者也有將該 image 上傳至 DockerHub，不用自己 Build。

不過我也有自己 Build 過一次，這邊提一下可能會遇到的問題。
(記得 Build 時要在這個 repo dir ! 該先下載的東西也請照 repo 寫的去找 !)

1. hue-4.3.0.tgz 我載不到阿 ? 
  - 這個你可以找到 Hue 的 repo，同個版本但是是 zip，可以解壓縮後再打包成 .tgz。
  - 但過程甚至還有權限問題 ... 有 Link 的關係吧，總之用管理員權限啟動壓縮程式就好。
2. 在 Run ``docker build -t hadoop3hbase-spark-hive .`` 時，Hue 會出現很多錯誤。
  - 沒錯，在 ``RUN make apps`` 會出現很多問題
  - 多裝了 java 8 (之前在 VPS 會出現這個問題)
  - 但是又會發現少了某個 Compiler。
  - 後來直接把跟 Hue 有關的 command 都註解掉了，因為我們也用不到 GUI。
3. 會發現缺少 ``scala-2.11.12.deb``。
  - ``wget www.scala-lang.org/files/archive/scala-2.11.12.deb`` 這行原本是註解掉的，先跑這個再跑一次吧。

這邊放上我自己改過的 Dockerfile，可以參考一下再 build。

```makefile Dockerfile
# HUE

# https://www.dropbox.com/s/auwpqygqgdvu1wj/hue-4.1.0.tgz
#ADD hue-4.3.0.tgz /

##
#RUN mv -f /hue-4.3.0 /opt/hue
#WORKDIR /opt/hue
#RUN make apps

#RUN chown -R hue:hue /opt/hue

... (中間有幾行)

#ADD hue.ini /opt/hue/desktop/conf

... (中間有幾行)

#    echo "PATH=$PATH:$HADOOP_HOME/bin:$HBASE_HOME/bin" >> ~/.bashrc

... (中間有幾行)

RUN wget www.scala-lang.org/files/archive/scala-2.11.12.deb 
# 上面這個取消註解
```

建了老半天發現 DockerHub 就能 pull 了，辛酸滿點。

Pull 完之後，執行 ``docker network create -d overlay --attachable my-attachable-network``

會出現

```bash
$ docker network create -d overlay --attachable my-attachable-network
Error response from daemon: This node is not a swarm manager. Use "docker swarm init" or "docker swarm join" to connect this node to swarm and try again.

```

解法 : ``docker swarm init``

再來是**按照順序**把 image run 起來。

- docker run --hostname=mysql --name mysql --network my-attachable-network -d sdwangntu/hive-metastore-db
- docker run --hostname=hadoop-master --name hadoop-master --network my-attachable-network -d sdwangntu/hadoop3hbase-spark-hive
- docker run --hostname=hadoop-worker --name hadoop-worker --network my-attachable-network -d sdwangntu/hadoop3hbase-spark-hive

如果遇到這樣的 error

```bash
$ docker run --hostname=mysql --name mysql --network  my-attachable-network -d sdwangntu/hive-metastore-db
docker: Error response from daemon: Conflict. The container name "/mysql" is already in use by container "59f0a2c30944b89cab5bb29214fcc04d31929bbc986fbb7939d34bddb448bbf1". You have to remove (or rename) that container to be able to reuse that name.
See 'docker run --help'.
```

那代表你已經 create 過了，請使用 start 的指令去喚醒他，或是直接用 Docker GUI 介面點擊 start。

小技巧 : 

```bash
$ docker ps -l
# 這樣才能看到"非"執行中的 container 
# 相關介紹 : https://www.jianshu.com/p/26f10054af50
```

建立完環境，network 弄好了，container 也都在 run 了 (一個 DB，一個 master，一個 worker)
如果我想要去做一些事情，這時再開一個 container 去搞。

``docker run --hostname=hadoop-dev --name hadoop-dev -v $(pwd):/home --network my-attachable-network -d sdwangntu/hadoop3hbase-spark-hive``

這個叫 hadoop-dev 的 container 就是我們要拿來和整個 Hadoop 系統溝通的地方。
值得注意的是，這指令有做 volumn 的連接，但是我在 windows 不起作用，也許有某些地方出問題。

現在有個問題，我們怎麼"進去" 這個 container ? 
用過 Docker 的應該知道就是 exec。

但在 windows 上會遇到一些問題，看 Log 說故事。

```bash
$ docker container exec -it hadoop-dev /bin/bash
the input device is not a TTY.  
If you are using mintty, try prefixing the command with 'winpty'

$ winpty docker container exec -it hadoop-dev /bin/bash
OCI runtime exec failed: exec failed: container_linux.go:349: starting container process caused "exec: \"C:/Program Files/Git/usr/bin/bash.exe\": stat 
C:/Program Files/Git/usr/bin/bash.exe: no such file or directory": unknown

$ winpty docker container exec -it hadoop-dev bash
# 成功進去了。
# 相關說明 : https://www.itread01.com/content/1547432471.html
```

進來之後做一些測試

```bash
root@hadoop-dev:/# yarn node -list
2020-11-04 09:35:23,603 INFO client.RMProxy: Connecting to ResourceManager at hadoop-master/10.0.1.2:
8032
Total Nodes:2
         Node-Id             Node-State Node-Http-Address       Number-of-Running-Containers
hadoop-worker:46787             RUNNING hadoop-worker:8042                                 0
hadoop-master:41117             RUNNING hadoop-master:8042                                 0
```

也順便跑了 ``hdfs dfsadmin -report`` 和 ``yarn jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.2.jar pi 4 10000``

妥。


# MapReduce 撰寫範例

這兩個程式我都有寫 read_from_file 的 function，用於 Local 測試。
不過，善用指令也能直接免寫檔測試 : 

``cat access.log | python mapper.py | python reducer.py``

``|`` 的功用就是會把 stdout 給 pipe 到下一個階段之中 (作為 stdin )。

請注意，檔案內最上面一定要寫這樣 : 
```python
#!/usr/bin/env python
# -*- coding: utf-8 -*
```

一個是告訴 mapreduce 你用的語言，一個是 Linux 編碼老問題。

## Python 版本

```python mapper.py
#!/usr/bin/env python
# -*- coding: utf-8 -*
import sys
import time
import datetime

def read_from_stdin():
	for line in sys.stdin:
		t = line.split('- - [')[1].split(' -')[0]
		time = datetime.datetime.strptime(t, "%d/%b/%Y:%H:%M:%S")
		print(time.strftime('%Y-%m-%d T %H:00:00.000')+"\t1") # %b: Month as locale’s abbreviated name.

read_from_stdin()
```


```python reducer.py
#!/usr/bin/env python
# -*- coding: utf-8 -*
import sys
import time
import datetime

def read_from_stdin():
	dic = {}
	for line in sys.stdin:
		t, count = line.split('\t', 1)
		if t in dic.keys():
			dic[t] += int(count)
		else:
			dic[t] = int(count)
	
	for k in dic:
		print(k+'\t'+str(dic[k]))

read_from_stdin()	
```

Log 範例 : 
```
64.242.88.10 - - [07/Mar/2004:16:05:49 -0800] "GET /twiki/bin/edit/Main/Double_bounce_sender?topicparent=Main.ConfigurationVariables HTTP/1.1" 401 12846
64.242.88.10 - - [07/Mar/2004:16:06:51 -0800] "GET /twiki/bin/rdiff/TWiki/NewUserTemplate?rev1=1.3&rev2=1.2 HTTP/1.1" 200 4523
64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] "GET /mailman/listinfo/hsdivision HTTP/1.1" 200 6291
64.242.88.10 - - [07/Mar/2004:16:11:58 -0800] "GET /twiki/bin/view/TWiki/WikiSyntax HTTP/1.1" 200 7352
64.242.88.10 - - [08/Mar/2004:06:57:09 -0800] "GET /twiki/bin/rdiff/TWiki/WebNotify HTTP/1.1" 200 11780
128.227.88.79 - - [08/Mar/2004:06:57:46 -0800] "GET /twiki/bin/view/Main/WebHome HTTP/1.1" 200 10419
128.227.88.79 - - [08/Mar/2004:06:57:46 -0800] "GET /twiki/pub/TWiki/TWikiLogos/twikiRobot46x50.gif HTTP/1.1" 304 -
```

單看其中一句 : 
```
64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] "GET /mailman/listinfo/hsdivision HTTP/1.1" 200 6291
```

每句格式都差不多，仔細觀察一下直接暴力切，最暴力無腦那種 ... 
然後別從中間的 `` -0800`` 下手，apache 在那邊會因為時區不一樣輸出不一樣
請從 ``[ ]`` 中括號去切。

Mapper 的輸出會是 : 
```
某某時間點 1
某某時間點 1
...
某某時間點 1
```

Reducer 就是去讀他們然後統計就好了。

## Java 版本

如果想要編譯成 jar 檔案來執行
必須寫在一個檔案內並且編譯時必須有 ``org.apache.hadoop`` 的包。

```java
import java.io.IOException;

import java.util.*;
import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class Parselog {

    public static class Map extends MapReduceBase implements
            Mapper<LongWritable, Text, Text, IntWritable> {

        @Override
        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {

                String line = value.toString();
                StringTokenizer tokenizer = new StringTokenizer(line);

                // Scanner scanner = new Scanner(System.in);
                while (tokenizer.hasMoreTokens()) {

                    String t = scanner.nextToken().split("- - \\[")[1].split(" -")[0];
                    SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", new Locale("es","ES"));
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:00:00.000\t1");
                    // %Y-%m-%d %H:00:00.000

                    Date result = null;
                    String result_s = null;
                    try {
                            result = accesslogDateFormat.parse(t);
                            result_s = outputDateFormat.format(result);
                            output.collect(result_s, new IntWritable(1)); //System.out.println(result_s);

                    }catch(ParseException e) {
                            e.printStackTrace();
                    }// End of Try Catch

                }// End of while

        }
    }

    public static class Reduce extends MapReduceBase implements
            Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterator<IntWritable> values,
                OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {

            Map<String, Integer> map = new HashMap<String, Integer>();

            // Scanner scanner = new Scanner(System.in);
            while (values.hasNext()) {

                String data[] = values.next().split("\t");

              // System.out.println("debug: " +  data[0] + ":" + data[1]);

              if( map.get(data[0]) != null ){
                map.put(data[0], map.get(data[0]) + 1);  
              }else{
                map.put(data[0], 1);
              }
            }// End of while

            for (String key : map.keySet()) {
              // use the key here
              output.collect(key+ ":" + map.get(key) , new IntWritable(sum)); //System.out.println(key+ ":" + map.get(key));

            }// End of for
            
        }
    }

    public static void main(String[] args) throws Exception {

        JobConf conf = new JobConf(WordCount.class);
        conf.setJobName("parselog");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);

    }
}
```

如果一樣分成 Mapper 和 Reducer 兩個檔案的話，範例如下

```java Mapper.java
import java.util.*;
import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Mapper {
  public static void main(String[] args){
  	
  	//String s = "64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] \"GET /mailman/listinfo/hsdivision HTTP/1.1\" 200 6291";

  	Scanner scanner = new Scanner(System.in);
  	while (scanner.hasNext()) {

	  	String t = scanner.nextLine().split("- - \\[")[1].split(" -")[0];
  		SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", new Locale("es","ES"));
  		SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:00:00.000\t1");
  		// %Y-%m-%d %H:00:00.000

  		Date result = null;
  		String result_s = null;
  		try {
  				result = accesslogDateFormat.parse(t);
  				result_s = outputDateFormat.format(result);
  				System.out.println(result_s);

		}catch(ParseException e) {
				e.printStackTrace();
		}// End of Try Catch

	}// End of while

  }//End of main
}//End of FirstJavaProgram Class
```

```java Reducer.java
import java.util.*;

public class Reducer {
  public static void main(String[] args){
  	
  	//String s = "64.242.88.10 - - [07/Mar/2004:16:10:02 -0800] \"GET /mailman/listinfo/hsdivision HTTP/1.1\" 200 6291";

    Map<String, Integer> map = new HashMap<String, Integer>();

  	Scanner scanner = new Scanner(System.in);
  	while (scanner.hasNext()) {

	  	String data[] = scanner.nextLine().split("\t");

      // System.out.println("debug: " +  data[0] + ":" + data[1]);

      if( map.get(data[0]) != null ){
        map.put(data[0], map.get(data[0]) + 1);  
      }else{
        map.put(data[0], 1);
      }
	}// End of while

  for (String key : map.keySet()) {
    // use the key here
    System.out.println(key+ ":" + map.get(key));

  }

  }//End of main
}//End of FirstJavaProgram Class
```

後面執行的語法搭配以下使用

```bash
hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -files Mapper.class,Reducer.class -input /workdir/log.txt -output /workdir_output -mapper "java Mapper" -reducer "java Reducer"

# 讓 MapReduce 在 Hadoop 上執行

這邊分成幾個步驟 : 

1. 放好 input
2. 放好 Code
3. 執行。

說實話，很簡單很單純，但是在指令不確定的時候，我的好幾個小時就噴了。

## 放 input

首先在 home 底下先把你的 Log (測資) 放進去吧。
我是直接開 vim 硬貼上 ... 不過建議不要這樣做。
後面也會提到其他可行的上傳方式。


放好之後，要上傳 input 到 HDFS 的中心
我參考 [Writing An Hadoop MapReduce Program In Python](https://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/)

但是某些目錄根本不存在於這個環境之中，這篇教學用不上了。

姑且這樣做試試看 : 
```bash
$dfs -copyFromLocal log.txt /user/hduser/worker
bash: dfs: command not found
```

這個 command 有兩個問題，一是後面目錄參數是指想要在 HDFS 上面放的位置
以及翻了一些講義，發現 dfs 不是一個程式，這是搭配在 hadoop 或是 hdfs 後面做使用的。

所以正確的流程應該是這樣 : 

```bash
root@hadoop-dev:/home# hdfs  dfs -mkdir /workdir
root@hadoop-dev:/home# hdfs  dfs -ls
# 沒東西是正常的
root@hadoop-dev:/home# hdfs dfs -copyFromLocal log.txt workdir
root@hadoop-dev:/home# hdfs dfs -ls
Found 1 items
-rw-r--r--   1 root supergroup     169706 2020-11-04 16:31 workdir
```

## 跑 MapReduce

這時候又有幾個問題 : 

1. Python 檔案怎麼上傳
2. 找不到 ``hadoop-*streaming*.jar`` 在哪裡啊
3. 指令到底怎麼打

以下依序解決。

### Python 檔案怎麼上傳

其實 docker 直接用 volumn 連接就好了，ubuntu 上很好用
但是 Windows 我沒成功

不過其實可以用 docker 內建的 cp 方式複製就好

``docker cp log.txt hadoop-dev:/home/``

這樣就會出現在  hadoop-dev 這個 Container 裡面的 /home 底下。


我自己當時做的時候繞了個遠路，上傳到了我個人架網站的 VPS 
(有 Server 可以讓網頁大家都能訪問到的地方)
然後直接 wget 下來。

以下一些鬼點子 : 
如果有工作站可以用，可以先傳到工作站，再用 scp 指令從工作站拉到本地。
或是上傳到 github，然後再把 repo 拉下來。

### 找不到 ``hadoop-*streaming*.jar`` 在哪裡啊

不管是網路上文章或是講義內的，目錄都不對
於是自己找 ..
(以下不用照做，單純紀錄)

1. 先 exec 進去 hadoop-master 
  - ``winpty docker container exec -it hadoop-master bash``
2. 然後 ``find / -iname 'hadoop' -type d``
  - type d 是 dictionary 的意思。
3. 然後發現大概是在 /opt/hadoop/share/hadoop 那邊，後來也挖到 compiler 了。


但其實關於這點，可以從 readme 的範例執行看到端睨 : 
``yarn jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.2.jar pi 4 10000``

當時想說這是 yarn 的指令就沒特別在意，
但其實這邊可以看到 hadoop 的位置位於 ``/opt/hadoop/share`` 就能進去挖挖看。

但總之結論就是這個 image 的 ``hadoop-*streaming*.jar`` 位置是在
``/opt/hadoop/share/hadoop/tools/lib`` 裡面。


### 指令到底怎麼打

以下是我各方搜尋嘗試過的

```bash
hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -mapper "python $PWD/mapper.py" -reducer "python $PWD/reducer.py" -input "/workdir/log.txt" -output "/workdir_output"
# 這個版本沒有用 -file


hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -file "$PWD/mapper.py"  -mapper "python $PWD/mapper.py"  -file "$PWD/reducer.py" -reducer "python $PWD/reducer.py" -input "/workdir/log.txt" -output "/workdir_output"
# 這個版本指令了 -file，位置參考講義內，同指令 python 檔案。

hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -file "$PWD/mapper.py"  -mapper "$PWD/mapper.py"  -file "$PWD/reducer.py" -reducer "$PWD/reducer.py" -input "/workdir/log.txt" -output "/workdir_output"
# 有些人執行在 -mapper 內是不用加上 python 的，嘗試看看。

hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -files "mapper.py,reducer.py" \
-input "/workdir/log.txt" \
-output "/workdir_output" \
-mapper "./mapper.py" \
-reducer "./reducer.py"
# 使用 -files 試試。目錄也改成 ./ 試試。

hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -file mapper.py -mapper ./mapper.py -file reducer.py -reducer ./reducer.py -input /workdir/log.txt -output /workdir_output
# 把雙引號拿掉試試。

hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -files mapper.py,reducer.py -input /workdir/log.txt -output /workdir_output -mapper "python mapper.py" -reducer "python reducer.py"
# 最終版本，參考某 stackoverflow 內解答其中一種。

```

這個過程花了非常多時間，也看過助教的範例但是不知怎地也不能 work
總之，在這個 image 底下，我是這樣成功的，給你參考。

```bash
hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-*streaming*.jar -files mapper.py,reducer.py -input /workdir/log.txt -output /workdir_output -mapper "python mapper.py" -reducer "python reducer.py"
```

國父革命 11 次成功，我這第 13 次才成功。


### hdfs workdir_output already exists Streaming Command Failed!

類似範例 : 

```bash
root@hadoop-dev:/home# hadoop jar (一長串指令)
packageJobJar: [/tmp/hadoop-unjar1232493226282012405/] [] /tmp/streamjob6347468080009358567.jar tmpDir=null
2020-11-04 20:22:46,709 INFO client.RMProxy: Connecting to ResourceManager at hadoop-master/10.0.1.5:8032
2020-11-04 20:22:46,826 INFO client.RMProxy: Connecting to ResourceManager at hadoop-master/10.0.1.5:8032
2020-11-04 20:22:46,903 ERROR streaming.StreamJob: Error Launching job : Output directory hdfs://hadoop-master:9000/workdir_output already exists
Streaming Command Failed!
```

如同他所寫，output dir 已經存在，所以要手動去刪掉。
這樣設計的原因是怕使用者沒有去備份 output 吧。

但不得不說手動刪挺麻煩的，我寫成 script 去跑。
或是你可以用指令 ``hdfs dfs -rm -r -f /workdir_output`` 去刪除。

```sh del.sh
#/bin/sh
hdfs dfs -rm -r -f /workdir_output
```

- 使用方式 : 
  - vim del.sh
    - 然後上面那些打進去
  - chmod 0755 del.sh
  - 想跑的時候輸入 ``./del.sh``


# 成果展示


## 執行 Log 截圖

![](https://i.imgur.com/97kl8lE.png)
![](https://i.imgur.com/yonJlLd.png)

![GIF](https://i.imgur.com/NKspe59.gif)

## 執行 Output 片段

![](https://i.imgur.com/3Ls6nVt.png)


# 心得

寫 mapper 和 reducer 的時間與小於弄環境以及搞指令，崩潰。

嗚嗚。

![](https://i.imgur.com/Sziftc9.png)

<div style="text-align: center">End</div>
-----------------------------------




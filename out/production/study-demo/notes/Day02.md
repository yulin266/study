# 1顺序
![[QQ20260726-153627.png]]
# 2内存
![[assets/87f64e0e983c96131c63ea3800f943b8.png]]
黄色和蓝色格子表示了位置和大小
# 3.变量
1.可以改变的向量存储
2.变量声明 数据类型   标识符
  变量赋值   
  变量使用前必须赋值，称为变量初始化。
# 4数据类型
1.比特（bit） 数据运算的最小储存单位
2.字节（byte） 数据的最小存储单位
1 byte = 8 bit

![[assets/60001d66b3ca22fc5de4c2fbf9ee02b9.png]]
byte  8
short 16
int     32
long   64
float f = 1.0F  
double 
小数点数据会默认为双精度
## 数据类型转换
小的可以自动变成大的![[assets/6b05d1e044fac6f75c52ab00783292f4.png|312]]
大变小 
`int i = （int）d；`
# 5.运算符 
![[assets/90c8fc58b346c9c70fb71434ab7f4d2b.png]]
## 1.0/2    1/2结果不一样
## 2.**最小的类型是int类型**
  `byte b1 = 10;`
  `byte b2 = 20;`
  `byte b3 = (byte)(b1 + b2);`
当执行  b1 + b2  这一步：
b1和b2原本都是byte，**相加运算前，JVM会把两个byte都转成int，相加结果最终是int类型。**
int占用4字节，比byte（1字节）范围大，属于「大类型往小类型赋值」，如果直接写
 byte b3 = b1 + b2;  编译器会直接爆红报错，因为存在精度丢失风险。
​
## 3. 强制转型 (byte) 的作用
 byte b3 = (byte)(b1 + b2); 
括号先算出int类型的总和，再手动强制把int转回byte类型，告诉编译器：我们人为接受精度截断，所以代码编译通过。
## 4 逻辑运算符
& 与    全真为true
|   或    有真为true
### .短路运算符
会根据第一个条件表达式判断是否执行第二个表达式
# 6 流程控制
## 1.顺序执行
先后顺序执行
## 2.分支执行
`int i = 10;`
`switch(i){`
`case 10 :`
     `sout10;`
`case 20 :`
    `sout20;`
` default:`
     `sout00; 
`}`
break跳出整个循环。
## 3.循环执环
while
do  while至少执行一次
for
break,跳出整个大循环，不再执行循环操作
continue 跳过当前循环，会执行之后的循环，
`println` 换行
print不换行



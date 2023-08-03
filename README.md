# Utility-sc

1, `git clone`する。

```
git clone https://github.com/oogasawa/Utility-sc
```

2, コンパイルする。

```
cd Utility-sc
mvn clean install assembly:single
```

3, 出来上がったfat jarをどこか決まった場所に置いておく。

```
cp target/Utility-sc-fat.jar ~/local/jars
```

4, ヘルプの表示（動作確認)

```
java -jar ~/local/jars/Utility-sc-fat.jar

$ java -jar target/Utility-sc-fat.jar
Parsing failed.  Reason: ERROR: No arguments.

java -jar utility-sc-fat.jar <command> <options>

The following is the usage of each command.

Print a table with respect to the elements with PMIDs
usage: paper:pmid_table -i <infile>
 -i,--infile <infile>   Input file (in TSV format with UTF-8 encoding)

Print an XML corresponding to the given Pubmed ID.
usage: paper:pubmed_xml -i <pmid> [-t <tag>]
 -i,--pmid <pmid>   A pubmed ID
 -t,--tag <tag>     An XML tag

Sort papers into meaingful categories
usage: paper:sort -i <infile>
 -i,--infile <infile>   Input file (in TSV format with UTF-8 encoding)

Convert a TSV table to a HTML table.
usage: tsv:toHtml -i <infile>
 -i,--infile <infile>   Input file
```

5, 例えばTSVをHTMLに変換する場合

```
java -jar ~/local/jars/Utility-sc-fat.jar tsv:toHtml -i your_data.txt > your_data.html
```

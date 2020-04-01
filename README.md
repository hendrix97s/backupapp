
<h1>Sistema de backup</h1>

<p>
A criação da aplicação surgiu a partir da necessidade de efetuar os backups do sistema DOCTORS OFFICE de todos os clientes “clinicas medicas”, para que pudesse manter a mesma integridade do sistema foi reaproveitado o próprio arquivo “DOCTORS.SEG” que contem a listagem dos arquivos necessários para backup.  
</p>
 
<h3>Como funciona o processo de backup? </h3>

<p>
Basicamente a aplicação de backup pega o arquivo DOCTORS.SEG do próprio sistema DCOTORS e efetua a leitura dos caminhos dos arquivos de dados do próprio sistema que serão copiados para um diretório temporário que por sua vez será compactado e transferido para o servidor web onde irá mostrar um gráfico de frequência de backup do respectivo cliente. 
</p>

<p><strong>Obs:</strong>  Esse backup pode ser configurado em dois horários certificando de que realmente o backup do dia irá ocorrer. </p>

 
<h3>Como configurar a aplicação </h3>

<p>
No diretório raiz do SO Windows deve-se criar um diretório chamado “SysBackup” e mover os arquivos da aplicação para dentro dele, abaixo segue a relação dos arquivos necessários: 
</p>

<ul>
    <li>Infos.txt </li>
    <li>Diretório  lib da aplicação </li>
    <li>BackupServer.jar</li>
</ul>    


<p>
O arquivo Infos.txt contém o caminho do sistema doctors, usuário (cliente) e os horários no qual será efetuado o backup. 
</p>

<h4>
Exemplo: 
</h4>

<p>
/teste/doctors <br/>

luiz <br/>

15:21-15:31 </br>
</p>

 

 
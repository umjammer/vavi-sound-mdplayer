import System;
import System.Collections.Generic;
import System.Windows.Forms;
import System.IO;

package WavMaker
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void btnRef_Click(Object sender, EventArgs e)
        {
            String fn = fileOpen();
            if (fn != null) tbFileName.Text = fn;
        }

        private void btnMake_Click(Object sender, EventArgs e)
        {
            if (make(tbFileName.Text))
            {
                MessageBox.Show("処理完了");
            }
            else
            {
                MessageBox.Show("処理失敗");
            }
        }

        private String fileOpen()
        {
            try
            {
                OpenFileDialog ofd = new OpenFileDialog();
                ofd.Filter = "すべてのファイル(*.*)|*.*";
                ofd.Title = "ファイルを選択してください";
                ofd.RestoreDirectory = true;
                ofd.CheckPathExists = true;
                ofd.Multiselect = false;

                if (ofd.ShowDialog() != DialogResult.OK)
                {
                    return null;
                }

                return ofd.FileNames[0];
            }
            catch { }
            return null;
        }

        private boolean make(String fn)
        {
            try
            {
                byte[] src = File.readAllBytes(fn);
                String dFn = Path.ChangeExtension(fn,".wav");
                List<byte> des=new ArrayList<byte>();

                // 'RIFF'
                des.add((byte)'R'); des.add((byte)'I'); des.add((byte)'F'); des.add((byte)'F');
                // サイズ
                int fsize = src.length + 36;
                des.add((byte)((fsize & 0xff) >> 0));
                des.add((byte)((fsize & 0xff00) >> 8));
                des.add((byte)((fsize & 0xff0000) >> 16));
                des.add((byte)((fsize & 0xff000000) >> 24));
                // 'WAVE'
                des.add((byte)'W'); des.add((byte)'A'); des.add((byte)'V'); des.add((byte)'E');
                // 'fmt '
                des.add((byte)'f'); des.add((byte)'m'); des.add((byte)'t'); des.add((byte)' ');
                // サイズ(16)
                des.add(0x10); des.add(0); des.add(0); des.add(0);
                // フォーマット(1)
                des.add(0x01); des.add(0x00);
                // チャンネル数(mono)
                des.add(0x01); des.add(0x00);
                //サンプリング周波数(8KHz)
                des.add(0x40); des.add(0x1f); des.add(0); des.add(0);
                //平均データ割合(8K)
                des.add(0x40); des.add(0x1f); des.add(0); des.add(0);
                //ブロックサイズ(1)
                des.add(0x01); des.add(0x00);
                //ビット数(8bit)
                des.add(0x08); des.add(0x00);

                // 'data'
                des.add((byte)'d'); des.add((byte)'a'); des.add((byte)'t'); des.add((byte)'a');
                // サイズ(データサイズ)
                des.add((byte)((src.length & 0xff) >> 0));
                des.add((byte)((src.length & 0xff00) >> 8));
                des.add((byte)((src.length & 0xff0000) >> 16));
                des.add((byte)((src.length & 0xff000000) >> 24));

                for (byte d : src) des.add(d);

                //出力
                File.WriteAllBytes(dFn, des.ToArray());

                return true;
            }
            catch
            {
            }
            return false;
        }
    }
}

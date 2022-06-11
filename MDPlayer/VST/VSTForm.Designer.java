package VST
{
    partial class VSTForm
    {
        /**
         * 必要なデザイナ変数です。
         */
        private System.ComponentModel.IContainer components = null;

        /**
         * 使用中のリソースをすべてクリーンアップします。
         */
         * <param name="disposing">マネージ リソースが破棄される場合 true、破棄されない場合は false です。</param>
        protected @Override void Dispose(boolean disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            super.Dispose(disposing);
        }

        // // #region Windows フォーム デザイナで生成されたコード

        /**
         * デザイナ サポートに必要なメソッドです。このメソッドの内容を
         * コード エディタで変更しないでください。
         */
        private void InitializeComponent()
        {
            this.SuspendLayout();
            // 
            // VSTForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(292, 266);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.Name = "VSTForm";
            this.ShowInTaskbar = false;
            this.Text = "VSTForm";
            this.Load += new System.EventHandler(this.VSTForm_Load);
            this.ResumeLayout(false);

        }

        // // #endregion
    }
}
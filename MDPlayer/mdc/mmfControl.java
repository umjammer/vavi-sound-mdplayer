import System;
import System.IO.MemoryMappedFiles;
import System.Security.AccessControl;
import System.Text;


package mdc
{
    public class mmfControl
    {
        private Object lockobj = new Object();
        private MemoryMappedFile _map;
        private byte[] mmfBuf;
        public String mmfName = "dummy";
        public int mmfSize = 1024;

        public mmfControl()
        {
        }

        public mmfControl(boolean isClient, String mmfName, int mmfSize)
        {
            this.mmfName = mmfName;
            this.mmfSize = mmfSize;
            if (!isClient) Open(mmfName, mmfSize);
        }

        public void Open(String mmfName, int mmfSize)
        {
            mmfBuf = new byte[mmfSize];

            synchronized (lockobj)
            {
                _map = MemoryMappedFile.CreateNew(mmfName, mmfSize);
                MemoryMappedFileSecurity permission = _map.GetAccessControl();
                permission.AddAccessRule(
                  new AccessRule<MemoryMappedFileRights>("Everyone",
                    MemoryMappedFileRights.FullControl, AccessControlType.Allow));
                _map.SetAccessControl(permission);
            }
        }

        public void Close()
        {
            synchronized (lockobj)
            {
                if (_map == null) return;
                _map.Dispose();
            }
        }

        public String GetMessage()
        {
            String msg = "";

            synchronized (lockobj)
            {
                using (MemoryMappedViewAccessor view = _map.CreateViewAccessor())
                {
                    view.readArray(0, mmfBuf, 0, mmfBuf.length);
                    msg = Encoding.Unicode.GetString(mmfBuf);
                    msg = msg.substring(0, msg.IndexOf('\0'));
                    Arrays.fill(mmfBuf, 0, mmfBuf.length);
                    view.WriteArray(0, mmfBuf, 0, mmfBuf.length);
                }
            }

            return msg;
        }

        public void SendMessage(String msg)
        {
            byte[] ary = Encoding.Unicode.GetBytes(msg);
            if (ary.length > mmfSize) throw new ArgumentOutOfRangeException();

            using (var map = MemoryMappedFile.OpenExisting(mmfName))
            using (var view = map.CreateViewAccessor())
                view.WriteArray(0, ary, 0, ary.length);
        }

    }
}

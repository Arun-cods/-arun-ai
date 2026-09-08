using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Threading;

namespace ArunAI
{
    class Program
    {
        static void Main(string[] args)
        {
            string url = "http://localhost:8080";
            string projectDir = @"c:\Users\asus\OneDrive\Desktop\arun-ai";
            string jarPath = Path.Combine(projectDir, @"target\arun-ai-0.0.1-SNAPSHOT.jar");

            // 1. Check if backend is already responding
            if (!IsServerRunning(url))
            {
                // Start backend quietly in the background using javaw.exe
                StartBackend(projectDir, jarPath);

                // Wait for server to become responsive
                int maxRetries = 25;
                for (int i = 0; i < maxRetries; i++)
                {
                    Thread.Sleep(800);
                    if (IsServerRunning(url))
                    {
                        break;
                    }
                }
            }

            // 2. Launch Arun AI in standalone Application Window mode
            LaunchAppWindow(url);
        }

        static bool IsServerRunning(string url)
        {
            try
            {
                HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
                request.Timeout = 1200;
                request.Method = "GET";
                using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
                {
                    return response.StatusCode == HttpStatusCode.OK;
                }
            }
            catch
            {
                return false;
            }
        }

        static void StartBackend(string projectDir, string jarPath)
        {
            try
            {
                string javawPath = @"C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\javaw.exe";
                if (!File.Exists(javawPath))
                {
                    javawPath = "javaw.exe";
                }

                ProcessStartInfo psi = new ProcessStartInfo();
                if (File.Exists(jarPath))
                {
                    psi.FileName = javawPath;
                    psi.Arguments = string.Format("-jar \"{0}\"", jarPath);
                }
                else
                {
                    psi.FileName = "cmd.exe";
                    psi.Arguments = "/c .\\mvnw.cmd spring-boot:run";
                }

                psi.WorkingDirectory = projectDir;
                psi.CreateNoWindow = true;
                psi.WindowStyle = ProcessWindowStyle.Hidden;
                psi.UseShellExecute = false;

                Process.Start(psi);
            }
            catch (Exception ex)
            {
                // Log or ignore
            }
        }

        static void LaunchAppWindow(string url)
        {
            string edgePath = @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe";
            if (!File.Exists(edgePath))
            {
                edgePath = @"C:\Program Files\Microsoft\Edge\Application\msedge.exe";
            }

            string chromePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe";
            if (!File.Exists(chromePath))
            {
                chromePath = @"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe";
            }

            string appDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), @"ArunAI\Profile");

            try
            {
                if (File.Exists(edgePath))
                {
                    ProcessStartInfo psi = new ProcessStartInfo
                    {
                        FileName = edgePath,
                        Arguments = string.Format("--app=\"{0}\" --window-name=\"Arun AI\" --user-data-dir=\"{1}\"", url, appDir),
                        UseShellExecute = true
                    };
                    Process.Start(psi);
                    return;
                }

                if (File.Exists(chromePath))
                {
                    ProcessStartInfo psi = new ProcessStartInfo
                    {
                        FileName = chromePath,
                        Arguments = string.Format("--app=\"{0}\" --user-data-dir=\"{1}\"", url, appDir),
                        UseShellExecute = true
                    };
                    Process.Start(psi);
                    return;
                }

                // Fallback to default browser
                Process.Start(new ProcessStartInfo(url) { UseShellExecute = true });
            }
            catch
            {
                Process.Start(new ProcessStartInfo(url) { UseShellExecute = true });
            }
        }
    }
}

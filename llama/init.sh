echo start

/bin/ollama serve &

if [ -f "/model/llama3-tuned.gguf" ]; then
  echo "model already downloaded"
else
  apt-get install -y curl
  curl -o /model/llama3-tuned.gguf -L https://huggingface.co/mansooooor/llama3-fine-tuned-gguf/resolve/main/llama3-fine-tuned-gguf-unsloth.Q4_K_M.gguf?download=true
  sleep 5
  ollama create llama3-tuned -f /llama/Modelfile
fi
ollama list

tail -f /dev/null

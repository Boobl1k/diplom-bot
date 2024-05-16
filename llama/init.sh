echo start

/bin/ollama serve &

if [ -f "/model/model.gguf" ]; then
  echo "model already downloaded"
else
  apt-get install -y curl
  curl -o /model/model.gguf -L https://huggingface.co/IlyaGusev/saiga_llama3_8b_gguf/resolve/main/model-q2_K.gguf?download=true
  sleep 5
  ollama create saiga -f /llama/Modelfile
fi
ollama list

tail -f /dev/null

"""Gera os efeitos sonoros do CandyMatch (WAV mono 16 bits) por síntese simples."""
import math, struct, sys, wave, random

TAXA = 44100

def envelope(t, dur, ataque=0.005, soltura=0.08):
    if t < ataque:
        return t / ataque
    if t > dur - soltura:
        return max(0.0, (dur - t) / soltura)
    return 1.0

def nota(freq, dur, vol=0.5, forma='seno', decaimento=6.0, glide=0.0):
    amostras = []
    fase = 0.0
    for i in range(int(TAXA * dur)):
        t = i / TAXA
        f = freq * (1 + glide * t / dur)
        fase += 2 * math.pi * f / TAXA
        if forma == 'seno':
            v = math.sin(fase) + 0.25 * math.sin(2 * fase)
        elif forma == 'quadrada':
            v = 0.6 if math.sin(fase) >= 0 else -0.6
        else:
            v = 2 * abs((fase / (2 * math.pi)) % 1 - 0.5) * 2 - 1  # triangular
        amostras.append(v * vol * math.exp(-decaimento * t) * envelope(t, dur))
    return amostras

def sequencia(notas, intervalo):
    total = []
    for k, (freq, dur, vol) in enumerate(notas):
        inicio = int(k * intervalo * TAXA)
        trecho = nota(freq, dur, vol)
        if len(total) < inicio + len(trecho):
            total.extend([0.0] * (inicio + len(trecho) - len(total)))
        for i, v in enumerate(trecho):
            total[inicio + i] += v
    return total

def gravar(caminho, amostras):
    pico = max(1e-9, max(abs(a) for a in amostras))
    escala = min(1.0, 0.9 / pico)
    with wave.open(caminho, 'wb') as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(TAXA)
        w.writeframes(b''.join(struct.pack('<h', int(a * escala * 32767)) for a in amostras))

pasta = sys.argv[1]
gravar(f'{pasta}/troca.wav', nota(520, 0.12, 0.5, 'triangular', 18, glide=0.5))
gravar(f'{pasta}/combinacao.wav', sequencia([(784, 0.25, 0.5), (1175, 0.3, 0.4)], 0.05))
gravar(f'{pasta}/invalida.wav', nota(160, 0.22, 0.45, 'quadrada', 8, glide=-0.3))
gravar(f'{pasta}/clique.wav', nota(900, 0.05, 0.4, 'seno', 40))
gravar(f'{pasta}/dica.wav', sequencia([(660, 0.15, 0.4), (880, 0.2, 0.4)], 0.09))
gravar(f'{pasta}/desfazer.wav', nota(700, 0.18, 0.45, 'triangular', 10, glide=-0.45))
gravar(f'{pasta}/vitoria.wav', sequencia([(523, 0.3, 0.45), (659, 0.3, 0.45), (784, 0.3, 0.45), (1047, 0.6, 0.5)], 0.12))
gravar(f'{pasta}/derrota.wav', sequencia([(392, 0.35, 0.45), (330, 0.35, 0.45), (262, 0.7, 0.5)], 0.22))
gravar(f'{pasta}/embaralhar.wav', sequencia([(400 + 120 * k, 0.08, 0.3) for k in range(8)], 0.035))

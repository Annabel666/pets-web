<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { agreeMessage, deleteMessage, loadArchive, loadListens, loadMessages, loadMine, loadSession, logout, postMessage, rememberListen, updateMessage } from "../api";

const archive = ref(null);
const messages = ref([]);
const notesMore = ref(false);
const listenNote = ref("");
const username = ref("");
const loadError = ref("");
const entered = ref(false);
const quoteQuery = ref("");
const playlistQuery = ref("");
const stageQuery = ref("");
const stageKind = ref("全部");
const stageYear = ref("");
const draft = ref("");
const messageError = ref("");
const messageScope = ref("all");
const replyTo = ref(null);
const replyDraft = ref("");
const editingId = ref(null);
const editDraft = ref("");
const confirmDeleteId = ref(null);
const sending = ref(false);
const recentIds = ref([]);
const openAlbum = ref("");
const copyLabel = ref("复制这首的地址");
const player = ref(null);
const current = ref(null);
const showPlaceholder = ref(true);
const statusMode = ref("");
const wantPlay = ref(false);
let loadTimer = 0;

const artist = computed(() => archive.value?.artist || {});
const voices = computed(() => archive.value?.voices || []);
const wave = computed(() => voices.value.filter((voice) => voice.list === "浪姐"));
const solo = computed(() => voices.value.filter((voice) => voice.list === "个人"));
function playlistHits(list) {
  const query = playlistQuery.value.trim().toLowerCase();
  return list
    .map((voice, index) => ({ voice, number: String(index + 1).padStart(2, "0") }))
    .filter((item) => {
      const hay = `${item.voice.title} ${item.voice.year} ${item.voice.note || ""}`.toLowerCase();
      return !query || hay.includes(query);
    });
}
const waveShown = computed(() => playlistHits(wave.value));
const soloShown = computed(() => playlistHits(solo.value));
const quotes = computed(() => {
  const query = quoteQuery.value.trim().toLowerCase();
  return (archive.value?.quotes || []).filter((item) => {
    const hay = `${item.text} ${item.source} ${item.year} ${(item.tags || []).join(" ")}`.toLowerCase();
    return !query || hay.includes(query);
  });
});
const kinds = computed(() => ["全部", ...new Set((archive.value?.stages || []).map((stage) => stage.kind))]);
const stages = computed(() => {
  const query = stageQuery.value.trim().toLowerCase();
  return (archive.value?.stages || []).filter((stage) => {
    const kindOk = stageKind.value === "全部" || stage.kind === stageKind.value;
    const hay = `${stage.date} ${stage.place} ${stage.venue} ${stage.title} ${stage.note || ""} ${stage.kind}`.toLowerCase();
    return kindOk && (!query || hay.includes(query));
  });
});
const stageYears = computed(() => {
  const groups = new Map();
  for (const stage of stages.value) {
    const year = String(stage.date).slice(0, 4);
    if (!groups.has(year)) groups.set(year, []);
    groups.get(year).push(stage);
  }
  return [...groups.entries()].reverse().map(([year, items]) => ({ year, stages: items }));
});
const shownYear = computed(() => {
  const years = stageYears.value.map((group) => group.year);
  if (years.includes(stageYear.value)) return stageYear.value;
  return years[0] || "";
});
const shownStages = computed(() => stageYears.value.find((group) => group.year === shownYear.value)?.stages || []);
const albums = computed(() => archive.value?.albums || []);
const recentVoices = computed(() =>
  recentIds.value.map((id) => voices.value.find((voice) => voice.id === id)).filter(Boolean)
);
const relatedStages = computed(() => {
  if (!current.value) return [];
  return (archive.value?.stages || []).filter((stage) => stage.voiceId === current.value.id);
});
const nextVoice = computed(() => {
  if (!current.value) return null;
  const list = current.value.list === "浪姐" ? wave.value : current.value.list === "个人" ? solo.value : [];
  const index = list.findIndex((voice) => voice.id === current.value.id);
  if (index < 0 || list.length < 2) return null;
  return list[(index + 1) % list.length];
});
function beijingDate(date) {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Shanghai",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

const nextShow = computed(() => {
  const today = beijingDate(new Date());
  return (archive.value?.stages || [])
    .filter((stage) => stage.kind === "礼物巡演" && /^\d{4}-\d{2}-\d{2}$/.test(stage.date) && stage.date >= today)
    .sort((a, b) => a.date.localeCompare(b.date))[0];
});
const birthdayNote = computed(() => {
  const born = /^(\d{4})-(\d{2})-(\d{2})$/.exec(artist.value.born || "");
  if (!born) return "";
  const month = Number(born[2]);
  const day = Number(born[3]);
  const [year, todayMonth, todayDay] = beijingDate(new Date()).split("-").map(Number);
  const today = Date.UTC(year, todayMonth - 1, todayDay);
  let next = Date.UTC(year, month - 1, day);
  if (next < today) next = Date.UTC(year + 1, month - 1, day);
  const days = Math.round((next - today) / 86400000);
  return {
    label: "生日倒计时",
    text: days === 0 ? "今天是曾沛慈的生日" : `距生日还有 ${days} 天`,
  };
});
const sourceUrl = computed(() => (current.value?.bvid ? `https://www.bilibili.com/video/${current.value.bvid}/` : "#"));
const searchUrl = computed(() => {
  const title = current.value?.title || "";
  return `https://search.bilibili.com/all?keyword=${encodeURIComponent("曾沛慈 " + title)}`;
});

function requestedSong() {
  try {
    return new URLSearchParams(location.search).get("song") || "";
  } catch (e) {
    return "";
  }
}

function rememberEntered() {
  try {
    return sessionStorage.getItem("pets-entered") === "1" || location.hash === "#guestbook" || Boolean(requestedSong());
  } catch (e) {
    return location.hash === "#guestbook" || Boolean(requestedSong());
  }
}

onMounted(async () => {
  entered.value = rememberEntered();
  try {
    const [data, notes, session] = await Promise.all([loadArchive(), loadMessages(), loadSession()]);
    archive.value = data;
    takeNotes(notes, false);
    username.value = session.username || "";
    try {
      localStorage.removeItem("pets-recent");
    } catch (e) {}
    if (username.value) {
      try {
        const recent = await loadListens();
        recentIds.value = Array.isArray(recent.ids) ? recent.ids : [];
      } catch (e) {}
    }
    current.value = (data.voices || []).find((voice) => voice.autoplay) || data.voices?.[0] || null;
    const requested = (data.voices || []).find((voice) => voice.id === requestedSong());
    if (requested) {
      try {
        sessionStorage.setItem("pets-entered", "1");
      } catch (e) {}
      document.documentElement.classList.add("entered");
      entered.value = true;
      playVoice(requested, true);
    }
  } catch (e) {
    loadError.value = "礼物厅暂时没有打开，稍后再来。";
  }
});

watch(current, (voice) => {
  if (voice && wantPlay.value) {
    wantPlay.value = false;
    playVoice(voice, true);
  }
});

function embedSrc(voice, autoplay) {
  const params = new URLSearchParams({
    isOutside: "true",
    bvid: voice.bvid,
    autoplay: autoplay ? "1" : "0",
    danmaku: "0",
    high_quality: "1",
  });
  return `https://player.bilibili.com/player.html?${params.toString()}`;
}

function revealPlayer() {
  const frame = document.getElementById("player-frame");
  if (!frame) return;
  const rect = frame.getBoundingClientRect();
  if (rect.top < 24 || rect.bottom > window.innerHeight - 8) {
    frame.scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

function setSongUrl(id) {
  const url = new URL(location.href);
  url.searchParams.set("song", id);
  history.replaceState(null, "", `${url.pathname}?${url.searchParams.toString()}${url.hash}`);
}

async function rememberRecent(id) {
  if (!username.value) return;
  const previous = recentIds.value.slice();
  recentIds.value = [id, ...recentIds.value.filter((item) => item !== id)].slice(0, 6);
  try {
    const recent = await rememberListen(id);
    if (Array.isArray(recent.ids)) recentIds.value = recent.ids;
    listenNote.value = "";
  } catch (e) {
    recentIds.value = previous;
    listenNote.value = "这首暂时没有记进最近听过。";
  }
}

function shareText(voice) {
  if (!voice?.title) {
    return {
      title: "曾沛慈 · 礼物厅",
      description: "曾沛慈的粉丝档案：听歌、查舞台、看专辑、留言。不是官方站点。",
    };
  }
  const note = voice.note ? ` · ${voice.note}` : "";
  const year = voice.year ? ` · ${voice.year}` : "";
  return {
    title: `${voice.title} · 曾沛慈 · 礼物厅`,
    description: `《${voice.title}》${year}${note}。粉丝整理的档案，不是官方站点。`,
  };
}

function setMeta(selector, value) {
  const node = document.head.querySelector(selector);
  if (node) node.setAttribute("content", value);
}

function publishShare(voice) {
  const share = shareText(voice);
  document.title = share.title;
  setMeta('meta[name="description"]', share.description);
  setMeta('meta[property="og:title"]', share.title);
  setMeta('meta[property="og:description"]', share.description);
}

function playVoice(voice, autoplay) {
  current.value = voice;
  publishShare(voice);
  if (voice?.id) {
    setSongUrl(voice.id);
    rememberRecent(voice.id);
  }
  revealPlayer();
  if (!voice.bvid) {
    player.value?.removeAttribute("src");
    showPlaceholder.value = true;
    statusMode.value = "";
    return;
  }
  showPlaceholder.value = false;
  statusMode.value = "loading";
  window.clearTimeout(loadTimer);
  loadTimer = window.setTimeout(() => {
    statusMode.value = "slow";
  }, 8000);
  if (player.value) {
    player.value.onload = () => {
      window.clearTimeout(loadTimer);
      statusMode.value = "";
    };
    player.value.src = embedSrc(voice, autoplay);
  }
}

function playNext() {
  if (nextVoice.value) playVoice(nextVoice.value, true);
}

function enterHall() {
  try {
    sessionStorage.setItem("pets-entered", "1");
  } catch (e) {}
  entered.value = true;
  document.documentElement.classList.add("entered");
  if (current.value) playVoice(current.value, true);
  else wantPlay.value = true;
}

async function signOut() {
  try {
    await logout();
  } catch (e) {}
  try {
    sessionStorage.removeItem("pets-entered");
  } catch (e) {}
  document.documentElement.classList.remove("entered");
  username.value = "";
  recentIds.value = [];
  listenNote.value = "";
  entered.value = false;
  messageScope.value = "all";
  replyTo.value = null;
  history.replaceState(null, "", "/");
  publishShare(null);
  await loadSession();
  notesMore.value = false;
  try {
    takeNotes(await loadMessages(), false);
  } catch (e) {}
}

function takeNotes(data, append) {
  const notes = Array.isArray(data?.notes) ? data.notes : [];
  messages.value = append ? messages.value.concat(notes) : notes;
  notesMore.value = Boolean(data?.more);
}

function tracksFor(album) {
  return (album.hits || []).map((hit) => ({
    hit,
    voices: voices.value.filter((voice) => voice.title === hit || voice.title.startsWith(`${hit} `)),
  }));
}

function toggleAlbum(title) {
  openAlbum.value = openAlbum.value === title ? "" : title;
}

async function copySong() {
  if (!current.value?.id) return;
  const url = `${location.origin}/?song=${encodeURIComponent(current.value.id)}`;
  try {
    await navigator.clipboard.writeText(url);
    copyLabel.value = "已复制";
  } catch (e) {
    copyLabel.value = "复制没有完成";
  }
  window.setTimeout(() => {
    copyLabel.value = "复制这首的地址";
  }, 1600);
}

async function refreshNotes() {
  takeNotes(messageScope.value === "mine" ? await loadMine() : await loadMessages(), false);
}

async function loadOlder() {
  const oldest = messages.value[messages.value.length - 1];
  if (!oldest || sending.value) return;
  messageError.value = "";
  sending.value = true;
  try {
    const data = messageScope.value === "mine" ? await loadMine(oldest.id) : await loadMessages(oldest.id);
    takeNotes(data, true);
  } catch (err) {
    messageError.value = err.message;
  } finally {
    sending.value = false;
  }
}

async function showAll() {
  messageScope.value = "all";
  messageError.value = "";
  try {
    takeNotes(await loadMessages(), false);
  } catch (err) {
    messageError.value = err.message;
  }
}

async function showMine() {
  messageScope.value = "mine";
  messageError.value = "";
  try {
    takeNotes(await loadMine(), false);
  } catch (err) {
    messageError.value = err.message;
  }
}

function listenStage(voiceId) {
  const voice = voices.value.find((item) => item.id === voiceId);
  if (voice) playVoice(voice, true);
}

async function sendNote() {
  messageError.value = "";
  sending.value = true;
  try {
    await postMessage(draft.value);
    draft.value = "";
    await refreshNotes();
  } catch (err) {
    messageError.value = err.message;
  } finally {
    sending.value = false;
  }
}

function openReply(note) {
  messageError.value = "";
  if (!username.value) {
    messageError.value = "登录后才能回一句。";
    return;
  }
  replyTo.value = replyTo.value === note.id ? null : note.id;
  replyDraft.value = "";
}

async function sendReply(note) {
  messageError.value = "";
  sending.value = true;
  try {
    await postMessage(replyDraft.value, note.id);
    replyDraft.value = "";
    replyTo.value = null;
    await refreshNotes();
  } catch (err) {
    messageError.value = err.message;
  } finally {
    sending.value = false;
  }
}

async function agree(note) {
  messageError.value = "";
  if (!username.value) {
    messageError.value = "登录后才能点赞。";
    return;
  }
  try {
    await agreeMessage(note.id);
    await refreshNotes();
  } catch (err) {
    messageError.value = err.message;
  }
}

function openEdit(item) {
  messageError.value = "";
  confirmDeleteId.value = null;
  if (editingId.value === item.id) {
    editingId.value = null;
    return;
  }
  editingId.value = item.id;
  editDraft.value = item.content;
}

async function saveEdit(item) {
  messageError.value = "";
  sending.value = true;
  try {
    await updateMessage(item.id, editDraft.value);
    editingId.value = null;
    editDraft.value = "";
    await refreshNotes();
  } catch (err) {
    messageError.value = err.message;
  } finally {
    sending.value = false;
  }
}

function askDelete(item) {
  messageError.value = "";
  editingId.value = null;
  confirmDeleteId.value = confirmDeleteId.value === item.id ? null : item.id;
}

async function removeItem(item) {
  messageError.value = "";
  sending.value = true;
  try {
    await deleteMessage(item.id);
    confirmDeleteId.value = null;
    await refreshNotes();
  } catch (err) {
    messageError.value = err.message;
  } finally {
    sending.value = false;
  }
}
</script>

<template>
  <div class="shell">
    <button class="gate" :class="{ hidden: entered }" type="button" @click="enterHall">
      <span class="kicker">PETS TSENG · 1984</span>
      <span class="giant">曾沛慈</span>
      <span class="gate-line">礼物厅。点一下，听《After Everything》。</span>
      <span class="gate-cta">进入礼物厅</span>
    </button>

    <aside class="rail">
      <a class="brand" href="#listen">
        <small>PETS TSENG</small>
        <strong>礼物厅</strong>
      </a>
      <nav>
        <a href="#listen">聆听</a>
        <a href="#playlists">歌单</a>
        <a href="#quotes">语句</a>
        <a href="#stages">舞台</a>
        <a href="#albums">专辑</a>
        <a href="#guestbook">留言</a>
      </nav>
      <p v-if="username && listenNote" class="quiet rail-note">{{ listenNote }}</p>
      <div v-if="username && recentVoices.length" class="recent">
        <span>最近听过</span>
        <button
          v-for="voice in recentVoices"
          :key="voice.id"
          type="button"
          :class="{ active: current && current.id === voice.id }"
          @click="playVoice(voice, true)"
        >
          {{ voice.title }}
        </button>
      </div>
      <div class="rail-account">
        <template v-if="username">
          <span>{{ username }}</span>
          <button class="exit" type="button" @click="signOut">退出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </aside>

    <main>
      <p v-if="loadError" class="form-error banner">{{ loadError }}</p>

      <section class="hero" id="listen">
        <div class="hero-copy">
          <p class="kicker">{{ artist.english || "Pets Tseng" }} · {{ artist.bornPlace }} · {{ artist.born }}</p>
          <div v-if="birthdayNote" class="birthday">
            <span>{{ birthdayNote.label }}</span>
            <strong>{{ birthdayNote.text }}</strong>
          </div>
          <h1>{{ artist.name || "曾沛慈" }}</h1>
          <p class="roles">{{ artist.rolesText }}</p>
          <p class="tagline">{{ artist.tagline }}</p>
          <p class="bio">{{ artist.bio }}</p>
          <a v-if="nextShow" class="next-show" href="#stages">
            <span>下一场</span>
            <strong>{{ nextShow.place }} · {{ nextShow.date }}</strong>
            <p>{{ nextShow.venue }} · {{ nextShow.title }}</p>
          </a>
          <dl v-if="archive" class="stats">
            <div><dt>{{ voices.length }}</dt><dd>首可播</dd></div>
            <div><dt>{{ archive.stages.length }}</dt><dd>场舞台</dd></div>
            <div><dt>{{ archive.quotes.length }}</dt><dd>则语句</dd></div>
            <div><dt>{{ archive.albums.length }}</dt><dd>张专辑</dd></div>
          </dl>
        </div>

        <div class="player-card" id="player-frame">
          <div class="player-top">
            <span>现在播放</span>
            <strong>{{ current ? current.title + " · " + current.year : "选择一首" }}</strong>
          </div>
          <div class="frame">
            <iframe
              ref="player"
              title="曾沛慈播放器"
              allow="autoplay; encrypted-media; fullscreen"
              allowfullscreen
              referrerpolicy="no-referrer-when-downgrade"
            ></iframe>
            <div v-if="showPlaceholder" class="placeholder">
              <template v-if="current && !current.bvid">
                这首暂时没有 B 站片源。<a :href="searchUrl" target="_blank" rel="noreferrer">在 B 站搜索</a>
              </template>
              <template v-else>点进礼物厅，或从歌单选一首。</template>
            </div>
          </div>
          <p v-if="statusMode === 'loading'" class="player-status">正在载入…若没有声音，点播放器里的播放键，或点「在哔哩哔哩打开」。</p>
          <p v-else-if="statusMode === 'slow'" class="player-status">
            若仍然没有声音，<a :href="sourceUrl" target="_blank" rel="noreferrer">用哔哩哔哩打开这首</a>。
          </p>
          <div class="player-links">
            <a class="source-link" :href="sourceUrl" target="_blank" rel="noreferrer">在哔哩哔哩打开</a>
            <button v-if="nextVoice" type="button" @click="playNext">再来一首</button>
            <button v-if="current" type="button" @click="copySong">{{ copyLabel }}</button>
          </div>
          <div v-if="relatedStages.length" class="related">
            <span>相关舞台</span>
            <p v-for="stage in relatedStages" :key="stage.id">
              {{ stage.date }} · {{ stage.place }} · {{ stage.venue }} · {{ stage.title }}
            </p>
          </div>
        </div>
      </section>

      <section class="section" id="playlists">
        <header class="section-head split">
          <div>
            <p class="kicker">PLAYLISTS</p>
            <h2>两栏歌单</h2>
          </div>
          <input v-model="playlistQuery" type="search" placeholder="搜索曲名、年份、注释" />
        </header>
        <div class="playlists">
          <article>
            <header>
              <h3>浪姐</h3>
              <p>乘风2026，按节目顺序。</p>
            </header>
            <button
              v-for="item in waveShown"
              :key="item.voice.id"
              class="track"
              :class="{ active: current && current.id === item.voice.id }"
              type="button"
              @click="playVoice(item.voice, true)"
            >
              <span class="num">{{ item.number }}</span>
              <span class="title">{{ item.voice.title }}</span>
              <span>{{ item.voice.year }} · {{ item.voice.note }}</span>
            </button>
            <p v-if="archive && playlistQuery.trim() && !waveShown.length" class="quiet">没有找到对应的曲目。</p>
          </article>
          <article>
            <header>
              <h3>个人歌单</h3>
              <p>她自己的歌，新的在前。</p>
            </header>
            <button
              v-for="item in soloShown"
              :key="item.voice.id"
              class="track"
              :class="{ active: current && current.id === item.voice.id }"
              type="button"
              @click="playVoice(item.voice, true)"
            >
              <span class="num">{{ item.number }}</span>
              <span class="title">{{ item.voice.title }}</span>
              <span>{{ item.voice.year }} · {{ item.voice.note }}</span>
            </button>
            <p v-if="archive && playlistQuery.trim() && !soloShown.length" class="quiet">没有找到对应的曲目。</p>
          </article>
        </div>
      </section>

      <section class="section" id="quotes">
        <header class="section-head split">
          <div>
            <p class="kicker">WORDS</p>
            <h2>说过的话</h2>
          </div>
          <input v-model="quoteQuery" type="search" placeholder="搜索语句、出处、年份" />
        </header>
        <div class="quote-grid">
          <article v-for="item in quotes" :key="item.id" class="quote">
            <p>「{{ item.text }}」</p>
            <div class="meta">{{ item.year }} · {{ item.source }}</div>
          </article>
        </div>
        <p v-if="archive && !quotes.length" class="quiet">没有找到对应的语句。</p>
      </section>

      <section class="section" id="stages">
        <header class="section-head split">
          <div>
            <p class="kicker">STAGES</p>
            <h2>走过的舞台</h2>
          </div>
          <input v-model="stageQuery" type="search" placeholder="搜索城市、场馆、曲目" />
        </header>
        <div class="chips">
          <button
            v-for="kind in kinds"
            :key="kind"
            class="chip"
            :class="{ active: stageKind === kind }"
            type="button"
            @click="stageKind = kind"
          >
            {{ kind }}
          </button>
        </div>
        <div v-if="stageYears.length" class="chips year-chips">
          <button
            v-for="group in stageYears"
            :key="group.year"
            class="chip"
            :class="{ active: shownYear === group.year }"
            type="button"
            @click="stageYear = group.year"
          >
            {{ group.year }}
          </button>
        </div>
        <div class="stage-list">
          <article v-for="stage in shownStages" :key="stage.id" class="stage">
            <time>{{ stage.date }}</time>
            <div>
              <b>{{ stage.title }}</b>
              <div class="meta">{{ stage.place }} · {{ stage.venue }}<template v-if="stage.note"> · {{ stage.note }}</template></div>
              <button v-if="stage.voiceId" class="listen" type="button" @click="listenStage(stage.voiceId)">听这场相关录音 →</button>
            </div>
            <span class="kind">{{ stage.kind }}</span>
          </article>
        </div>
        <p v-if="archive && !stages.length" class="quiet">没有找到对应的舞台。</p>
      </section>

      <section class="section" id="albums">
        <header class="section-head">
          <p class="kicker">ALBUMS</p>
          <h2>五张专辑</h2>
        </header>
        <div class="album-grid">
          <article v-for="album in albums" :key="album.title" class="album" :class="{ open: openAlbum === album.title }">
            <button class="album-toggle" type="button" @click="toggleAlbum(album.title)">
              <div class="meta">{{ album.year }} · {{ album.label }}</div>
              <h3>{{ album.title }}</h3>
              <div class="meta">{{ openAlbum === album.title ? "收起" : (album.hits || []).join(" / ") }}</div>
            </button>
            <div v-if="openAlbum === album.title" class="album-tracks">
              <template v-for="row in tracksFor(album)" :key="row.hit">
                <button
                  v-for="voice in row.voices"
                  :key="voice.id"
                  type="button"
                  :class="{ active: current && current.id === voice.id }"
                  @click="playVoice(voice, true)"
                >
                  {{ voice.title }}
                </button>
                <span v-if="!row.voices.length" class="meta">{{ row.hit }}</span>
              </template>
            </div>
          </article>
        </div>
      </section>

      <section class="section letter" id="guestbook">
        <header class="section-head">
          <p class="kicker">NOTES</p>
          <h2>粉丝留言</h2>
        </header>
        <p v-if="messageError" class="form-error">{{ messageError }}</p>
        <form v-if="username" class="message-form" @submit.prevent="sendNote">
          <textarea v-model="draft" maxlength="500" rows="4" placeholder="写给曾沛慈，或写给同来的人。"></textarea>
          <button class="primary" type="submit" :disabled="sending">送出留言</button>
        </form>
        <p v-else class="quiet">
          <router-link to="/login">登录</router-link>后可以留言。还没有名字的话，先<router-link to="/register">注册</router-link>。
        </p>
        <div v-if="username" class="chips">
          <button class="chip" :class="{ active: messageScope === 'all' }" type="button" @click="showAll">全部</button>
          <button class="chip" :class="{ active: messageScope === 'mine' }" type="button" @click="showMine">我写过的</button>
        </div>
        <div v-if="messages.length" class="notes">
          <article v-for="note in messages" :key="note.id" class="note">
            <header>
              <b>{{ note.username }}</b>
              <time>{{ note.createdAt }}</time>
            </header>
            <p v-if="editingId !== note.id">{{ note.content }}</p>
            <form v-else class="reply-form" @submit.prevent="saveEdit(note)">
              <textarea v-model="editDraft" maxlength="500" rows="3"></textarea>
              <button class="primary" type="submit" :disabled="sending">保存</button>
            </form>
            <div class="note-actions">
              <button type="button" :class="{ on: note.agreed }" @click="agree(note)">
                {{ note.agreed ? "已赞" : "点赞" }}<template v-if="note.agreeCount"> {{ note.agreeCount }}</template>
              </button>
              <button type="button" @click="openReply(note)">回一句</button>
              <template v-if="note.owned">
                <button type="button" @click="openEdit(note)">修改</button>
                <button v-if="confirmDeleteId !== note.id" type="button" @click="askDelete(note)">删除</button>
                <span v-else class="confirm-delete">
                  删掉这句和下面的回复？
                  <button type="button" @click="removeItem(note)">删掉</button>
                  <button type="button" @click="confirmDeleteId = null">留下</button>
                </span>
              </template>
            </div>
            <div v-if="note.replies && note.replies.length" class="replies">
              <article v-for="reply in note.replies" :key="reply.id" class="reply">
                <header>
                  <b>{{ reply.username }}</b>
                  <time>{{ reply.createdAt }}</time>
                </header>
                <p v-if="editingId !== reply.id"><span v-if="reply.replyTo" class="reply-target">回复 {{ reply.replyTo }}：</span>{{ reply.content }}</p>
                <form v-else class="reply-form" @submit.prevent="saveEdit(reply)">
                  <textarea v-model="editDraft" maxlength="200" rows="2"></textarea>
                  <button class="primary" type="submit" :disabled="sending">保存</button>
                </form>
                <div class="note-actions">
                  <button type="button" :class="{ on: reply.agreed }" @click="agree(reply)">
                    {{ reply.agreed ? "已赞" : "点赞" }}<template v-if="reply.agreeCount"> {{ reply.agreeCount }}</template>
                  </button>
                  <button type="button" @click="openReply(reply)">回一句</button>
                  <template v-if="reply.owned">
                    <button type="button" @click="openEdit(reply)">修改</button>
                    <button v-if="confirmDeleteId !== reply.id" type="button" @click="askDelete(reply)">删除</button>
                    <span v-else class="confirm-delete">
                      删掉这句？
                      <button type="button" @click="removeItem(reply)">删掉</button>
                      <button type="button" @click="confirmDeleteId = null">留下</button>
                    </span>
                  </template>
                </div>
                <form v-if="replyTo === reply.id" class="reply-form" @submit.prevent="sendReply(reply)">
                  <textarea v-model="replyDraft" maxlength="200" rows="2" :placeholder="`回复 ${reply.username}`"></textarea>
                  <button class="primary" type="submit" :disabled="sending">送出</button>
                </form>
              </article>
            </div>
            <form v-if="replyTo === note.id" class="reply-form" @submit.prevent="sendReply(note)">
              <textarea v-model="replyDraft" maxlength="200" rows="2" placeholder="回一句。"></textarea>
              <button class="primary" type="submit" :disabled="sending">送出</button>
            </form>
          </article>
        </div>
        <button v-if="notesMore" class="chip more-notes" type="button" :disabled="sending" @click="loadOlder">更早的留言</button>
        <p v-if="archive && !messages.length && messageScope === 'mine'" class="quiet">你还没有写过。</p>
        <p v-else-if="archive && !messages.length" class="quiet">还没有留言。</p>
      </section>

      <footer>
        <p>粉丝整理的档案，不是官方站点。</p>
      </footer>
    </main>
  </div>
</template>

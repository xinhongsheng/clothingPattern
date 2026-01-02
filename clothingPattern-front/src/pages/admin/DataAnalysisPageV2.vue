<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import LeftOne from '@/components/LeftOne.vue'
import LeftTwo from '@/components/LeftTwo.vue'
import LeftThree from '@/components/LeftThree.vue'
import CenterOne from '@/components/CenterOne.vue'
import CenterTwo from '@/components/CenterTwo.vue'
import RightOne from '@/components/RightOne.vue'
import RightTwo from '@/components/RightTwo.vue'
import RightThree from '@/components/RightThree.vue'

// 定义一个响应式变量来存储当前时间
const currentTime = ref(getCurrentDateTime())

function getCurrentDateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = (now.getMonth() + 1).toString().padStart(2, '0')
  const day = now.getDate().toString().padStart(2, '0')
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  const seconds = now.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

let timer = null

// 在组件挂载时启动定时器
onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = getCurrentDateTime()
  }, 1000)
})

// 在组件卸载时清除定时器
onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<template>
  <div class="home">
    <header class="top">
      <dv-border-box-11 title="数据分析中心" class="top-nav" :color="['#188ffe']">
        <div class="header-info">
          <div class="name">创作人：小辛同学</div>
          <div class="time">{{ currentTime }}</div>
        </div>
      </dv-border-box-11>
    </header>

    <main class="bottom">
      <div class="left">
        <dv-border-box-1 class="left-item">
          <LeftOne />
        </dv-border-box-1>
        <dv-border-box-1 class="left-item">
          <LeftTwo />
        </dv-border-box-1>
        <dv-border-box-1 class="left-item">
          <LeftThree />
        </dv-border-box-1>
      </div>

      <div class="center">
        <dv-border-box-1 class="center-top">
          <div class="center-top-item">
            <div class="center-top-item-span">
              <span>当前总用户量</span>
              <CenterOne type="user" />
            </div>

            <div class="line"></div>

            <div class="center-top-item-span">
              <span>图案总数</span>
              <CenterOne type="pattern" />
            </div>
          </div>
        </dv-border-box-1>
        <dv-border-box-1 class="center-bottom">
          <CenterTwo />
        </dv-border-box-1>
      </div>

      <div class="right">
        <dv-border-box-1 class="right-item">
          <RightOne />
        </dv-border-box-1>

        <dv-border-box-1 class="right-item">
          <RightTwo />
        </dv-border-box-1>

        <dv-border-box-1 class="right-item">
          <RightThree />
        </dv-border-box-1>
      </div>
    </main>
  </div>
</template>

<style scoped lang="less">
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.home {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background-color: #000;
  color: #fff;
}

.top {
  display: grid;
  height: 60px;
  flex-shrink: 0;
  .top-nav {
    width: 100%;
    height: 100%;
    position: relative;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);

    .header-info {
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 100px;
    }

    .name {
      color: #00f2ff;
      text-shadow:
        0 0 5px #00f2ff,
        0 0 40px #00f2ff;
      font-weight: 700;
      font-size: 0.9rem;
    }
    .time {
      font-weight: 700;
      color: #00f2ff;
      text-shadow:
        0 0 5px #00f2ff,
        0 0 40px #00f2ff;
      font-size: 0.9rem;
    }
  }
}

.bottom {
  flex: 1;
  display: grid;
  grid-template-columns: 3fr 6fr 3fr;
  gap: 10px;
  padding: 10px;
}

.left {
  display: grid;
  grid-template-rows: repeat(3, 1fr);
  gap: 10px;
  .left-item {
    width: 100%;
    border-radius: 8px;
    height: 100%;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

.center {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
  flex-direction: column;

  flex-direction: column;
  justify-content: space-between;
  .center-top {
    display: flex;
    width: 100%;
    height: 23%;
    background-color: #000;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);

    .center-top-item {
      display: flex;
      width: 100%;
      height: 100%;
      justify-content: space-around;
      align-items: center;

      .line {
        display: flex;
        width: 5px;
        height: 30%;
        background-color: #3be1c4;
      }

      .center-top-item-span {
        display: flex;
        height: 100%;
        align-items: center;
        justify-content: center;
        flex-wrap: wrap;
        flex-direction: column;
        span {
          display: flex;
          width: 100%;
          justify-content: center;
          align-items: center;
          font-weight: 700;
          color: #00f2ff;
          text-shadow: 0 0 5px #00f2ff;
        }
      }
    }
  }

  .center-bottom {
    display: flex;
    width: 100%;
    height: 75%;
    background-color: #000;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

.right {
  display: grid;
  grid-template-rows: repeat(3, 1fr);
  gap: 10px;
  .right-item {
    width: 100%;
    height: 100%;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

/* 响应式适配 */
@media (max-width: 992px) {
  .home {
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }

  .bottom {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto;
    padding: 8px;
    gap: 8px;
  }

  .left {
    grid-column: 1;
    grid-row: 1;
  }

  .center {
    grid-column: 2;
    grid-row: 1 / 2;
  }

  .right {
    grid-column: 1 / 3;
    grid-row: 2;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: 1fr;
  }

  .right .right-item {
    min-height: 200px;
  }

  .left .left-item {
    min-height: 180px;
  }

  .center .center-top {
    height: 120px;
    min-height: 120px;
  }

  .center .center-bottom {
    height: 350px;
    min-height: 350px;
  }
}

@media (max-width: 768px) {
  .home {
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }

  .top {
    height: auto;
    min-height: 50px;

    .top-nav {
      .header-info {
        position: static;
        flex-direction: column;
        justify-content: center;
        padding: 8px 15px;
        gap: 2px;
      }

      .name {
        font-size: 0.65rem;
      }

      .time {
        font-size: 0.6rem;
      }
    }
  }

  .bottom {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 8px;
  }

  .left {
    display: flex;
    flex-direction: column;
    gap: 8px;
    order: 2;

    .left-item {
      min-height: 220px;
      height: auto;
    }
  }

  .center {
    order: 1;
    gap: 8px;

    .center-top {
      height: auto;
      min-height: 110px;
      padding: 10px 0;

      .center-top-item {
        flex-direction: row;
        flex-wrap: nowrap;
        padding: 0 10px;

        .center-top-item-span {
          flex: 1;
          min-width: 0;
          padding: 5px;

          span {
            font-size: 0.75rem;
            white-space: nowrap;
          }
        }

        .line {
          height: 50px;
          width: 2px;
          flex-shrink: 0;
        }
      }
    }

    .center-bottom {
      height: auto;
      min-height: 280px;
    }
  }

  .right {
    display: flex;
    flex-direction: column;
    gap: 8px;
    order: 3;

    .right-item {
      min-height: 220px;
      height: auto;
    }
  }
}

@media (max-width: 576px) {
  .top {
    min-height: 45px;

    .top-nav {
      .header-info {
        padding: 6px 10px;
      }

      .name {
        font-size: 0.55rem;
      }

      .time {
        font-size: 0.5rem;
      }
    }
  }

  .bottom {
    padding: 6px;
    gap: 6px;
  }

  .left .left-item,
  .right .right-item {
    min-height: 200px;
  }

  .center {
    gap: 6px;

    .center-top {
      min-height: 100px;
      padding: 8px 0;

      .center-top-item {
        padding: 0 8px;

        .center-top-item-span {
          span {
            font-size: 0.65rem;
          }
        }

        .line {
          height: 40px;
        }
      }
    }

    .center-bottom {
      min-height: 250px;
    }
  }
}

@media (max-width: 375px) {
  .top {
    min-height: 40px;

    .top-nav {
      .header-info {
        padding: 5px 8px;
      }

      .name {
        font-size: 0.5rem;
      }

      .time {
        font-size: 0.45rem;
      }
    }
  }

  .bottom {
    padding: 5px;
    gap: 5px;
  }

  .left .left-item,
  .right .right-item {
    min-height: 180px;
  }

  .center {
    .center-top {
      min-height: 90px;

      .center-top-item {
        padding: 0 5px;

        .center-top-item-span {
          span {
            font-size: 0.55rem;
          }
        }

        .line {
          height: 30px;
        }
      }
    }

    .center-bottom {
      min-height: 220px;
    }
  }
}
</style>

/**
 * 随机趣味用户名生成器
 */

// 趣味昵称库
const nicknames = {
  // 古风武侠类
  wuxia: [
    '落魄山右护法', '二狗子', '剑圣传人', '江湖百晓生', '桃花岛主',
    '丐帮九袋长老', '昆仑派弃徒', '剑阁守门人', '铸剑山庄少主',
    '武林盟主隔壁', '峨眉派扫地僧', '青城山大侠', '武当山闲散道士',
    '少林寺伙夫', '逍遥谷隐士', '绝情谷遗孤', '神雕侠友'
  ],

  // 可爱动物类
  animals: [
    '小熊猫饲养员', '流浪猫观察家', '哈士奇政委', '柯基腿短',
    '树袋熊睡觉', '企鹅向前冲', '刺猬拥抱困难', '狐狸的狡黠',
    '兔子不吃草', '松鼠的松果', '考拉醒不醒', '柴犬的微笑',
    '猫咪保安', '小猪佩奇', '大笨象会跳舞', '狐狸精不精'
  ],

  // 网络流行语类
  internet: [
    '键盘侠退休了', '潜水员冒泡', '吃瓜群众', '打酱油的路人',
    '摸鱼达人', '加班狂魔', '熬夜冠军', '起床困难户',
    '奶茶续命', '减肥失败', '剁手党', '脱发小能手',
    '佛系青年', '社畜翻身', '咸鱼翻身', '躺平冠军'
  ],

  // 古诗词类
  poetry: [
    '采菊东篱下', '悠然见南山', '举杯邀明月', '对影成三人',
    '春风十里', '不如你', '人间四月天', '面朝大海',
    '春暖花开', '且听风吟', '陌上花开', '缓缓归矣',
    '星垂平野', '月涌大江', '大漠孤烟', '长河落日'
  ],

  // 奇幻魔法类
  fantasy: [
    '霍格沃茨留级生', '中土旅行者', '守望先锋先锋', '龙与地下城DM',
    '魔法学徒', '精灵王子迷弟', '矮人金库钥匙', '巨龙饲养员',
    '巫师学徒', '指环王粉丝', '纳尼亚传奇', '亚特兰蒂斯寻宝',
    '魔法部员工', '凤凰社成员', '食死徒叛徒', '傲罗办公室'
  ],

  // 科学技术类
  tech: [
    '404找不到', '程序猿媛', '运维小哥', '产品汪汪',
    '设计狮妹妹', '测试工程师', '前端切图工', '后端API',
    '全栈练习生', '算法工程师', '数据分析师', '区块链信徒',
    '人工智能小白', '云计算用户', '物联网设备', '5G体验官'
  ],

  // 美食类
  food: [
    '火锅狂魔', '奶茶半糖去冰', '烧烤摊主', '夜宵大排档',
    '小龙虾十三香', '麻辣烫不加麻', '奶茶不加糖', '西瓜中间一口',
    '鸡蛋灌饼', '煎饼果子加两个蛋', '肉夹馍要肥的', '螺蛳粉不加笋',
    '美食博主', '黑暗料理界', '米其林弃徒', '路边摊常客'
  ],

  // 运动游戏类
  sports: [
    '电竞少年', 'LOL青铜', 'DOTA2选手', '王者荣耀王者',
    '吃盒子的吃鸡', '永劫无间玩家', '原神启动', '塞尔达传说',
    '马里奥大叔', '皮卡丘十万伏特', '健身爱好者', '马拉松路边',
    '篮球板凳', '足球守门员', '游泳健将', '登山队队长'
  ]
}

/**
 * 从数组中随机选择一个元素
 */
function randomChoice<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]
}

/**
 * 生成随机趣味用户名
 * @returns 随机用户名
 */
export function generateRandomNickname(): string {
  // 随机选择一个类别
  const categories = Object.keys(nicknames) as Array<keyof typeof nicknames>
  const randomCategory = randomChoice(categories)

  // 从该类别中随机选择一个昵称
  return randomChoice(nicknames[randomCategory])
}

/**
 * 生成指定数量的不重复随机用户名
 * @param count 生成数量
 * @returns 用户名数组
 */
export function generateRandomNicknames(count: number): string[] {
  const allNicknames = Object.values(nicknames).flat()
  const shuffled = [...allNicknames].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, Math.min(count, allNicknames.length))
}

/**
 * 生成带数字后缀的随机用户名（避免重复）
 * @param existingNicknames 已存在的用户名列表
 * @returns 不重复的随机用户名
 */
export function generateUniqueNickname(existingNicknames: string[]): string {
  let nickname = generateRandomNickname()
  let suffix = 1

  while (existingNicknames.includes(nickname)) {
    nickname = `${generateRandomNickname()}_${suffix}`
    suffix++
  }

  return nickname
}

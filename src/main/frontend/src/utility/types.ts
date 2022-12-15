type ChildProps = { children?: React.ReactNode }

// Article
type ArticleInfo = {
  articleId: number,
  username: string,
  title: string,
  content: string,
  createdAt: string,
  updatedAt?: string,
  isWritten?: boolean
};

type PostArticle = {
  id? : string,
  title: string,
  content: string
}

export type { ChildProps, ArticleInfo, PostArticle };